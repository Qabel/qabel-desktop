package de.qabel.desktop.daemon.sync;

import de.qabel.box.storage.AbstractNavigation;
import de.qabel.box.storage.LocalReadBackend;
import de.qabel.box.storage.LocalWriteBackend;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.blacklist.PatternBlacklist;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncer;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.storage.cache.CachedBoxVolumeImpl;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SyncIntegrationTest {
    private static final Logger logger = AbstractControllerTest.createLogger();
    public static final long TIMEOUT = 10000L;
    protected Path remoteDir;
    protected Path tmpDir1;
    protected Path tmpDir2;
    private DefaultSyncer syncer1;
    private DefaultSyncer syncer2;
    private Thread managerThread1;
    private Thread managerThread2;
    private CachedBoxVolumeImpl volume1;
    private CachedBoxVolumeImpl volume2;
    private MonitoredTransferManager manager1;
    private MonitoredTransferManager manager2;
    private BoxSyncConfig config1;
    private BoxSyncConfig config2;
    private PatternBlacklist blacklist;

    @Before
    public void setUp() {
        AbstractNavigation.DEFAULT_AUTOCOMMIT_DELAY = 0L;
        try {
            remoteDir = Files.createTempDirectory(getClass().getSimpleName());
            tmpDir1 = Files.createTempDirectory(getClass().getSimpleName());
            tmpDir2 = Files.createTempDirectory(getClass().getSimpleName());
        } catch (Exception e) {
            fail("failed to create tmp dir: " + e.getMessage());
        }

        try {
            blacklist = new PatternBlacklist();
            blacklist.add(Pattern.compile("\\..*\\.qpart~"));
            blacklist.add(Pattern.compile("\\..*~"));

            Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).build();
            Account account = new Account("a", "b", "c");
            config1 = new DefaultBoxSyncConfig(
                "config1",
                tmpDir1,
                BoxFileSystem.get("/sync"),
                identity,
                account,
                new SqliteSyncIndexFactory()
            );
            config2 = new DefaultBoxSyncConfig(
                "config2",
                tmpDir2,
                BoxFileSystem.get("/sync"),
                identity,
                account,
                new SqliteSyncIndexFactory()
            );
            LocalReadBackend readBackend = new LocalReadBackend(remoteDir);
            LocalWriteBackend writeBackend = new LocalWriteBackend(remoteDir);
            volume1 = new CachedBoxVolumeImpl(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
            volume2 = new CachedBoxVolumeImpl(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
            volume1.createIndex("qabel", "prefix");
            volume1.navigate().createFolder("sync");
            volume2.navigate().createFolder("sync");
            manager1 = new MonitoredTransferManager(new DefaultTransferManager());
            manager2 = new MonitoredTransferManager(new DefaultTransferManager());

            syncer1 = new DefaultSyncer(config1, volume1, manager1);
            syncer1.setLocalBlacklist(blacklist);
            syncer1.getUploadFactory().setSyncDelayMills(0L);

            syncer2 = syncer2();

            syncer1.setPollInterval(100, TimeUnit.MILLISECONDS);
            syncer1.run();
            syncer1.waitFor();
            managerThread1 = new Thread(manager1);
            managerThread1.start();

            managerThread2 = new Thread(manager2);
            managerThread2.start();
        } catch (Exception e) {
            logger.error("failed to start sync", e);
            fail("failed to start sync: " + e.getMessage());
        }
    }

    private DefaultSyncer syncer2() {
        DefaultSyncer syncer2;
        syncer2 = new DefaultSyncer(config2, volume2, manager2);
        syncer2.setLocalBlacklist(blacklist);
        syncer2.getUploadFactory().setSyncDelayMills(0L);
        syncer2.setPollInterval(100, TimeUnit.MILLISECONDS);
        return syncer2;
    }

    @After
    public void tearDown() throws InterruptedException {
        syncer1.shutdown();
        syncer2.shutdown();
        managerThread1.interrupt();
        managerThread2.interrupt();

        try {
            FileUtils.deleteDirectory(remoteDir.toFile());
        } catch (IOException e) {
            logger.error("failed to delete temporary remote dir " + remoteDir, e);
        }
        try {
            FileUtils.deleteDirectory(tmpDir1.toFile());
        } catch (IOException e) {
            logger.error("failed to delete temporary dir " + tmpDir1, e);
        }
        try {
            FileUtils.deleteDirectory(tmpDir2.toFile());
        } catch (IOException e) {
            logger.error("failed to delete temporary dir " + tmpDir2, e);
        }
    }

    protected static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
        waitUntil(evaluate, TIMEOUT, errorMessage);
    }

    protected static void waitUntil(Callable<Boolean> evaluate) {
        long timeout = TIMEOUT;
        waitUntil(evaluate, timeout);
    }

    protected static void waitUntil(Callable<Boolean> evaluate, long timeout) {
        waitUntil(evaluate, timeout, null);
    }

    protected static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
        try {
            long startTime = System.currentTimeMillis();
            while (!evaluate.call()) {
                Thread.yield();
                if (System.currentTimeMillis() - timeout > startTime) {
                    fail("wait timeout: " + (errorMessage == null ? "" : errorMessage.call()));
                }
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void syncsFileInSubdirectory() throws Exception {
        syncer2.run();
        syncer2.waitFor();

        Path dir1 = Paths.get(tmpDir1.toString(), "dir");
        Files.createDirectories(dir1);

        Path dir2 = Paths.get(tmpDir2.toString(), "dir");
        waitUntil(() -> Files.isDirectory(dir2), TIMEOUT);

        Path file1 = Paths.get(dir1.toString(), "file");
        Files.write(file1, "text".getBytes());

        Path file2 = Paths.get(dir2.toString(), "file");
        waitUntil(() -> Files.exists(file2), TIMEOUT);
        waitUntil(() -> Files.size(file2) > 0);
        waitUntil(() -> "text".equals(new String(Files.readAllBytes(file2))));

        List<Transaction> history = manager2.getHistory();

        // first comes the sync root creation (download OR upload first)
        int rootSyncUpload = 1;
        if (history.get(0) instanceof Upload) {
            rootSyncUpload = 0;
        }
        assertEquals(BoxFileSystem.get("/sync").toString(), history.get(rootSyncUpload).getDestination().toString());

        assertTrue(history.get(1) instanceof Download);
        assertEquals(BoxFileSystem.get("/sync/dir").toString(), history.get(1).getSource().toString());

        waitUntil(() -> history.size() > 2, () -> {
            SyncIntegrationTest i = this;
            return "too few events: " + history;
        });
        for (int i = 2; i < history.size(); i++) {
            if (history.get(i) instanceof Download) {
                assertEquals(BoxFileSystem.get("/sync/dir/file").toString(), history.get(i).getSource().toString());
                return;
            }
        }
        assertTrue(
                "an unecpected " + history.get(2) + " occured after DOWNLAOD /sync/dir (" + history.size() + " in history)",
                history.get(2) instanceof Download
        );
        fail("no download found");
    }

    @Test
    public void syncsDeleteOccuredLocallyDuringOfflinePeriod() throws Exception {
        syncer2.run();
        syncer2.waitFor();

        Path path1 = Paths.get(tmpDir1.toString(), "file");
        File file1 = path1.toFile();
        file1.createNewFile();
        waitUntil(() -> manager1.getTransactions().size() == 0);
        waitUntil(() -> volume1.navigate().navigate("sync").hasFile("file"), TIMEOUT);

        Path path2 = Paths.get(tmpDir2.toString(), "file");
        File file2 = path2.toFile();
        waitUntil(() -> volume2.navigate().navigate("sync").hasFile("file"));
        waitUntil(file2::exists);
        syncer2.shutdown();
        syncer2.join();
        config2.getSyncIndex().clear();

        // delete file while syncer2 is offline
        assertTrue(file2.delete());

        // (re-)start syncer2 to detect a delete and upload the delete
        syncer2 = syncer2();
        syncer2.run();
        syncer2.waitFor();
        waitUntil(() -> !volume2.navigate().navigate("sync").hasFile("file"));
        waitUntil(() -> !volume1.navigate().navigate("sync").hasFile("file"));

        waitUntil(() -> !file1.exists());
    }

    @Test
    public void syncsDeleteOccuredRemotelyDuringOfflinePeriod() throws Exception {
        Path path = Paths.get(tmpDir2.toString(), "file");
        File file = path.toFile();
        file.createNewFile();

        // mark file up2date on syncer2
        SyncState uploadedState = new SyncState(true, file.lastModified(), file.length());
        String relativeDir = config2.getLocalPath().relativize(tmpDir2).toString();
        BoxPath remoteFilePath = config2.getRemotePath().resolve(relativeDir).resolve("file");
        config2.getSyncIndex().get(remoteFilePath).setSyncedState(uploadedState);
        syncer2.run();
        syncer2.waitFor();

        // file is missing remotely but was uploaded (according to index) => it has been deleted remotely
        waitUntil(() -> !file.exists());
    }
}
