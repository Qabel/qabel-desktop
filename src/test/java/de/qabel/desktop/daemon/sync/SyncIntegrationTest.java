package de.qabel.desktop.daemon.sync;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncer;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.storage.LocalReadBackend;
import de.qabel.desktop.storage.LocalWriteBackend;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SyncIntegrationTest {
	public static final long TIMEOUT = 10000L;
	protected Path remoteDir;
	protected Path tmpDir1;
	protected Path tmpDir2;
	private DefaultSyncer syncer1;
	private DefaultSyncer syncer2;
	private Thread managerThread1;
	private Thread managerThread2;
	private CachedBoxVolume volume1;
	private CachedBoxVolume volume2;
	private MonitoredTransferManager manager1;
	private MonitoredTransferManager manager2;
	private BoxSyncConfig config1;
	private BoxSyncConfig config2;

	@Before
	public void setUp() {
		try {
			remoteDir = Files.createTempDirectory(getClass().getSimpleName());
			tmpDir1 = Files.createTempDirectory(getClass().getSimpleName());
			tmpDir2 = Files.createTempDirectory(getClass().getSimpleName());
		} catch (Exception e) {
			fail("failed to create tmp dir: " + e.getMessage());
		}

		try {
			Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).build();
			Account account = new Account("a", "b", "c");
			config1 = new DefaultBoxSyncConfig("config1", tmpDir1, Paths.get("/sync"), identity, account);
			config2 = new DefaultBoxSyncConfig("config2", tmpDir2, Paths.get("/sync"), identity, account);
			LocalReadBackend readBackend = new LocalReadBackend(remoteDir);
			LocalWriteBackend writeBackend = new LocalWriteBackend(remoteDir);
			volume1 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume2 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume1.createIndex("qabel", "prefix");
			volume1.navigate().createFolder("sync");
			manager1 = new MonitoredTransferManager(new DefaultTransferManager());
			manager2 = new MonitoredTransferManager(new DefaultTransferManager());

			syncer1 = new DefaultSyncer(config1, volume1, manager1);
			syncer1.getUploadFactory().setSyncDelayMills(0L);

			this.syncer2 = syncer2();

			syncer1.setPollInterval(100, TimeUnit.MILLISECONDS);
			syncer1.run();
			syncer1.waitFor();
			managerThread1 = new Thread(manager1);
			managerThread1.start();

			managerThread2 = new Thread(manager2);
			managerThread2.start();
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to start sync: " + e.getMessage());
		}
	}

	private DefaultSyncer syncer2() {
		DefaultSyncer syncer2;
		syncer2 = new DefaultSyncer(config2, volume2, manager2);
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
			e.printStackTrace();
		}
		try {
			FileUtils.deleteDirectory(tmpDir1.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileUtils.deleteDirectory(tmpDir2.toFile());
		} catch (IOException e) {
			e.printStackTrace();
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
		final SyncIntegrationTest test = this;
		waitUntil(() -> {
			final SyncIntegrationTest test2 = test;
			return Files.isDirectory(dir2);
		}, TIMEOUT);

		Path file1 = Paths.get(dir1.toString(), "file");
		Files.write(file1, "text".getBytes());

		Path file2 = Paths.get(dir2.toString(), "file");
		waitUntil(() -> Files.exists(file2), TIMEOUT);
		assertEquals("text", new String(Files.readAllBytes(file2)));

		List<Transaction> history = manager2.getHistory();

		// first comes the sync root creation (download OR upload first)
		int rootSyncUpload = 1;
		if (history.get(0) instanceof Upload) {
			rootSyncUpload = 0;
		}
		assertEquals(Paths.get("/sync"), history.get(rootSyncUpload).getDestination());

		assertTrue(history.get(1) instanceof Download);
		assertEquals(Paths.get("/sync/dir"), history.get(1).getSource());

		waitUntil(() -> history.size() > 2, () -> {
			SyncIntegrationTest i = SyncIntegrationTest.this;
			return "too few events: " + history;
		});
		assertTrue(
				"an unecpected " + history.get(2) + " occured after DOWNLAOD /sync/dir",
				history.get(2) instanceof Download
		);
		assertEquals(Paths.get("/sync/dir/file"), history.get(2).getSource());
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

		// delete file while syncer2 is offline
		file2.delete();

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
		config2.getSyncIndex().update(Paths.get(tmpDir2.toString(), "file"), file.lastModified(), true);
		syncer2.run();
		syncer2.waitFor();

		// file is missing remotely but was uploaded (according to index) => it has been deleted remotely
		waitUntil(() -> !file.exists());
	}
}
