package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.*;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.Download;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.blacklist.PatternBlacklist;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.CREATE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DefaultSyncerTest extends AbstractSyncTest {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private MonitoredTransferManager manager;
    private BoxSyncConfig config;
    private Identity identity;
    private Account account;
    private DefaultSyncer syncer;
    private BoxNavigation nav;
    private BoxVolumeImpl volume;

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
        config = new DefaultBoxSyncConfig(
            tmpDir,
            BoxFileSystem.get("/"),
            identity,
            account,
            new SqliteSyncIndexFactory()
        );

        Path storage = Files.createTempDirectory("tmp");
        volume = new BoxVolumeImpl(
            new LocalReadBackend(storage),
            new LocalWriteBackend(storage),
            identity.getPrimaryKeyPair(),
            new byte[0],
            storage.toFile(),
            "prefix"
        );
        volume.createIndex("indexexexex");
        nav = volume.navigate();
        syncer = new DefaultSyncer(config, volume, manager);
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

        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 2);
    }

    @Test
    public void createsLocalDirIfNotExisting() throws Exception {
        Path newDir = tmpDir.resolve("subdir");
        config.setLocalPath(newDir);
        syncer.run();

        waitUntil(() -> Files.isDirectory(newDir));
    }

    @Test
    public void afterStopFilesAreNotAddedAsUploads() throws Exception {
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
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1 && syncer.isPolling());
        syncer.stop();
        assertFalse(syncer.isPolling());
    }

    @Test
    public void stopStopsTransactions() throws Exception {
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1, () -> manager.getTransactions().toString());
        syncer.stop();
        manager.cleanup();
        assertEquals(0, manager.getTransactions().size());
    }

    @Test
    public void stopStopsRemoteWacher() throws Exception {
        syncer.run();

        waitUntil(() -> manager.getTransactions().size() == 1 && syncer.isPolling());
        syncer.stop();
        manager.getHistory().clear();

        push(new RemoteChangeEvent(Paths.get("/c"), false, 1000L, CREATE, new BoxFile("a", "b", "c", 1L, 2L, new byte[0], null, null), nav));

        // wait for something to not happen?
        Thread.sleep(100);
        assertThat(manager.getHistory(), is(empty()));
    }

    @Test
    public void addsFoldersAsDownloads() throws Exception {
        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.MILLISECONDS);
        syncer.run();

        waitUntil(syncer::isPolling);
        push(new RemoteChangeEvent(Paths.get("/tmp/someFolder"), true, 1000L, CREATE, null, nav));

        waitUntil(() -> manager.getTransactions().size() == 2);
        Transaction transaction = manager.getTransactions().get(0) instanceof Download ? manager.getTransactions().get(0) : manager.getTransactions().get(1);
        assertEquals("/tmp/someFolder", transaction.getSource().toString());
        assertEquals(0.0, syncer.getProgress(), 0.001);
    }

    @Test
    public void addsRemoteDeletesAsDownload() throws Exception {
        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.MILLISECONDS);
        syncer.run();

        waitUntil(syncer::isPolling);
        push(new RemoteChangeEvent(Paths.get("/tmp/someFile"), false, 1000L, DELETE, null, nav));

        waitUntil(() -> manager.getTransactions().size() == 2);
        Transaction transaction = manager.getTransactions().get(0) instanceof Download ? manager.getTransactions().get(0) : manager.getTransactions().get(1);
        assertEquals("/tmp/someFile", transaction.getSource().toString());
        assertEquals(Transaction.TYPE.DELETE, transaction.getType());
    }

    protected void push(RemoteChangeEvent event) {
        try {
            executor.submit(() -> syncer.remoteChangeHandler.update(null, event)).get();
        } catch (Exception e) {
            System.err.println("failed to push event: " + e.getMessage());
        }
    }

    @Test
    public void policyPreventsActionsOutsideOfTheSyncDir() throws Exception {
        syncer = new DefaultSyncer(config, volume, manager);
        syncer.setPollInterval(1, TimeUnit.DAYS);
        syncer.run();

        waitUntil(syncer::isPolling);
        push(new RemoteChangeEvent(Paths.get("../../usr/local/tmp"), true, 1000L, DELETE, null, nav));

        manager.getTransactions().stream().filter(transaction -> transaction instanceof Download).findFirst()
            .ifPresent(transaction -> fail("policy should have prevented delete but " + transaction + " happened"));
    }

    @Test
    public void bootstrappingCreatesDirIfLocalDirDoesNotExist() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());

        syncer = new DefaultSyncer(config, volume, manager);
        syncer.run();
        assertTrue(Files.isDirectory(tmpDir));
    }

    @Test
    public void ignoresFilesOnBlacklist() throws Exception {
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
