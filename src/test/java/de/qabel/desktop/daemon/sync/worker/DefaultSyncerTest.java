package de.qabel.desktop.daemon.sync.worker;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.blacklist.Blacklist;
import de.qabel.desktop.daemon.sync.blacklist.PatternBlacklist;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static org.junit.Assert.*;

public class DefaultSyncerTest extends AbstractSyncTest {
    private MonitoredTransferManager manager;
    private BoxSyncConfig config;
    private Identity identity;
    private Account account;
    private DefaultSyncer syncer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000")).factory().build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        account = new Account("a", "b", "c");
        manager = new MonitoredTransferManager(new DefaultTransferManager());
        config = new DefaultBoxSyncConfig(tmpDir, Paths.get("/"), identity, account, new InMemorySyncIndexFactory());
    }

    @Override
    @After
    public void tearDown() throws InterruptedException {
        if (syncer != null) {
            syncer.shutdown();
        }
        super.tearDown();
    }

    @Test
    public void addsFilesAsUploads() throws IOException {
        new File(tmpDir.toFile(), "file").createNewFile();

        syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 2);
    }

    @Test
    public void afterStopFilesAreNotAddedAsUploads() throws Exception {
        syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1);
        syncer.stop();
        File file = new File(tmpDir.toFile(), "file");
        file.createNewFile();

        // pre restart
        waitUntil(() -> manager.getTransactions().size() == 1, () -> "expected 1 transaction but got " + manager.getTransactions().size() + ": " + manager.getTransactions());
        assertContainsTransaction(config.getLocalPath());

        // restart syncer
        syncer.startWatcher();
        // after restart (everything is re-notified)
        waitUntil(() -> manager.getTransactions().size() > 2);

        assertContainsTransaction(config.getLocalPath());
        assertContainsTransaction(file.toPath());
        assertEquals(3, manager.getTransactions().size());
    }

    public void assertContainsTransaction(Path localPath) {
        for (Transaction t : manager.getTransactions()) {
            if (t.getSource().equals(localPath)) {
                assertTrue(true);
                return;
            }
        }
        fail(localPath + " not found in " + manager.getTransactions());
    }

    @Test
    public void stopStopsPolling() throws Exception {
        syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1 && syncer.isPolling());
        syncer.stop();
        assertFalse(syncer.isPolling());
    }

    @Test
    public void stopStopsTransactions() throws Exception {
        syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1);
        syncer.stop();
        manager.cleanup();
        assertEquals(0, manager.getTransactions().size());
    }

    @Test
    public void addsFoldersAsDownloads() throws Exception {
        BoxNavigationStub nav = new BoxNavigationStub(null, null);
        nav.event = new RemoteChangeEvent(Paths.get("/tmp/someFolder"), true, 1000L, ChangeEvent.TYPE.CREATE, null, nav);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = nav;
        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.MILLISECONDS);
        syncer.run();

        waitUntil(syncer::isPolling);

        waitUntil(() -> manager.getTransactions().size() == 2);
        Transaction transaction = manager.getTransactions().get(0) instanceof Download ? manager.getTransactions().get(0) : manager.getTransactions().get(1);
        assertEquals("/tmp/someFolder", transaction.getSource().toString());
        assertEquals(0.0, syncer.getProgress(), 0.001);
    }

    @Test
    public void addsRemoteDeletesAsDownload() throws Exception {
        BoxNavigationStub nav = new BoxNavigationStub(null, null);
        nav.event = new RemoteChangeEvent(Paths.get("/tmp/someFile"), false, 1000L, DELETE, null, nav);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = nav;

        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.MILLISECONDS);
        syncer.run();

        waitUntil(syncer::isPolling);

        waitUntil(() -> manager.getTransactions().size() == 2);
        Transaction transaction = manager.getTransactions().get(0) instanceof Download ? manager.getTransactions().get(0) : manager.getTransactions().get(1);
        assertEquals("/tmp/someFile", transaction.getSource().toString());
        assertEquals(Transaction.TYPE.DELETE, transaction.getType());
    }

    @Test
    public void policyPreventsActionsOutsideOfTheSyncDir() throws Exception {
        BoxNavigationStub nav = new BoxNavigationStub(null, null);
        nav.event = new RemoteChangeEvent(Paths.get("../../usr/local/tmp"), true, 1000L, DELETE, null, nav);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = nav;
        final List<Object> events = new LinkedList<>();
        nav.addObserver((o, arg) -> events.add(arg));

        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.DAYS);
        syncer.run();

        waitUntil(syncer::isPolling);
        waitUntil(() -> !events.isEmpty());

        manager.getTransactions().stream().filter(transaction -> transaction instanceof Download).findFirst()
            .ifPresent(transaction -> fail("policy should have prevented delete but " + transaction + " happened"));
    }

    @Test
    public void bootstrappingCreatesDirIfLocalDirDoesNotExist() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());

        syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
        syncer.run();
        assertTrue(Files.isDirectory(tmpDir));
    }

    @Test
    public void ignoresFilesOnBlacklist() throws Exception {
        BoxNavigationStub nav = new BoxNavigationStub(null, null);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = nav;

        PatternBlacklist blacklist = new PatternBlacklist();
        blacklist.add(Pattern.compile("\\..*\\.qpart~"));
        blacklist.add(Pattern.compile("\\..*~"));
        BlacklistSpy spy = new BlacklistSpy(blacklist);

        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setLocalBlacklist(spy);
        syncer.setPollInterval(1, TimeUnit.DAYS);
        syncer.run();
        syncer.waitFor();

        Files.write(tmpDir.resolve(".mydownload.qpart~"), "content".getBytes());

        waitUntil(() -> manager.getTransactions().size() > 0);  // wait for root sync event
        waitUntil(() -> !spy.tests.isEmpty());
        waitUntil(() -> !syncer.isProcessingLocalEvents());
        assertTrue(manager.getTransactions().get(0).isDir());
        assertEquals("blacklisted file was not ignored: " + manager.getTransactions(), 1, manager.getTransactions().size());
    }

    @Test
    public void ignoresFoldersOnBlacklist() throws Exception {
        BoxNavigationStub nav = new BoxNavigationStub(null, null);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = nav;

        PatternBlacklist blacklist = new PatternBlacklist();
        blacklist.add(Pattern.compile(".*\\.*"));
        BlacklistSpy spy = new BlacklistSpy(blacklist);

        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setLocalBlacklist(spy);
        syncer.setPollInterval(1, TimeUnit.DAYS);
        syncer.run();
        syncer.waitFor();

        Path illegalFolder = tmpDir.resolve("illegal\\folder");
        Files.createDirectory(illegalFolder);
        Files.write(illegalFolder.resolve("validFile"), "content".getBytes());

        waitUntil(() -> manager.getTransactions().size() > 0);  // wait for root sync event
        waitUntil(() -> !spy.tests.isEmpty());
        waitUntil(() -> !syncer.isProcessingLocalEvents());
        assertTrue(manager.getTransactions().get(0).isDir());
        assertEquals("blacklisted folder was not ignored: " + manager.getTransactions(), 1, manager.getTransactions().size());
    }
}
