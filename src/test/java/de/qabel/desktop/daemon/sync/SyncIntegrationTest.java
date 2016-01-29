package de.qabel.desktop.daemon.sync;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncer;
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
	protected Path remoteDir;
	protected Path tmpDir1;
	protected Path tmpDir2;
	private DefaultSyncer syncer1;
	private DefaultSyncer syncer2;
	private Thread managerThread1;
	private Thread managerThread2;
	private CachedBoxVolume volume1;
	private CachedBoxVolume volume2;
	private TransferManager manager1;
	private TransferManager manager2;
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
			config1 = new DefaultBoxSyncConfig(tmpDir1, Paths.get("/sync"), identity, account);
			config2 = new DefaultBoxSyncConfig(tmpDir2, Paths.get("/sync"), identity, account);
			LocalReadBackend readBackend = new LocalReadBackend(remoteDir);
			LocalWriteBackend writeBackend = new LocalWriteBackend(remoteDir);
			volume1 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume2 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume1.createIndex("qabel", "prefix");
			volume1.navigate().createFolder("sync");
			manager1 = new DefaultTransferManager();
			manager2 = new DefaultTransferManager();

			syncer1 = new DefaultSyncer(config1, volume1, manager1);
			syncer1.getUploadFactory().setSyncDelayMills(0L);
			syncer2 = new DefaultSyncer(config2, volume2, manager2);
			syncer2.getUploadFactory().setSyncDelayMills(0L);

			syncer1.setPollInterval(1, TimeUnit.MILLISECONDS);
			syncer1.run();
			syncer1.waitFor();
			managerThread1 = new Thread(manager1);
			managerThread1.start();

			syncer2.setPollInterval(1, TimeUnit.MILLISECONDS);
			managerThread2 = new Thread(manager2);
			managerThread2.start();
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to start sync: " + e.getMessage());
		}
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
		waitUntil(evaluate, 1000L, errorMessage);
	}

	protected static void waitUntil(Callable<Boolean> evaluate) {
		long timeout = 1000L;
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
		});

		Path file1 = Paths.get(dir1.toString(), "file");
		Files.write(file1, "text".getBytes());

		Path file2 = Paths.get(dir2.toString(), "file");
		waitUntil(() -> Files.exists(file2));
		assertEquals("text", new String(Files.readAllBytes(file2)));

		List<Transaction> history = manager2.getHistory();

		// first comes the sync root creation (download OR upload first)
		int rootSyncUpload = 1;
		int rootSyncDownload = 0;
		if (history.get(0) instanceof Upload) {
			rootSyncUpload = 0;
			rootSyncDownload = 1;
		}
		assertEquals(Paths.get("/sync"), history.get(rootSyncUpload).getDestination());

		assertTrue(history.get(rootSyncDownload) instanceof Download);
		assertEquals(Paths.get("/sync"), history.get(rootSyncDownload).getSource());

		assertTrue(history.get(2) instanceof Download);
		assertEquals(Paths.get("/sync/dir"), history.get(2).getSource());

		waitUntil(() -> history.size() > 3, () -> "too few events: " + history);
		assertTrue(
				"an unecpected " + history.get(3) + " occured after DOWNLAOD /sync/dir",
				history.get(3) instanceof Download
		);
		assertEquals(Paths.get("/sync/dir/file"), history.get(3).getSource());
	}

	@Test
	public void syncsDeleteOccuredLocallyDuringOfflinePeriod() throws Exception {
		Path path = Paths.get(tmpDir1.toString(), "file");
		File file = path.toFile();
		file.createNewFile();
		waitUntil(() -> manager1.getTransactions().size() == 0);

		// mark file up2date on syncer2
		waitUntil(() -> volume1.navigate().navigate("sync").hasFile("file"));
		long mtime = volume2.navigate().navigate("sync").getFile("file").mtime;
		config2.getSyncIndex().update(Paths.get(tmpDir2.toString(), "file"), mtime, true);
		syncer2.run();
		syncer2.waitFor();

		waitUntil(() -> !file.exists());
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
