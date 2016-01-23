package de.qabel.desktop.daemon.sync;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.management.DefaultLoadManager;
import de.qabel.desktop.daemon.management.LoadManager;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
	private LoadManager manager1;
	private LoadManager manager2;

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
			BoxSyncConfig config1 = new DefaultBoxSyncConfig(tmpDir1, Paths.get("/sync"), identity, account);
			BoxSyncConfig config2 = new DefaultBoxSyncConfig(tmpDir2, Paths.get("/sync"), identity, account);
			LocalReadBackend readBackend = new LocalReadBackend(remoteDir);
			LocalWriteBackend writeBackend = new LocalWriteBackend(remoteDir);
			volume1 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume2 = new CachedBoxVolume(readBackend, writeBackend, identity.getPrimaryKeyPair(), new byte[0], new File(System.getProperty("java.io.tmpdir")), "prefix");
			volume1.createIndex("qabel", "prefix");
			volume1.navigate().createFolder("sync");
			manager1 = new DefaultLoadManager();
			manager2 = new DefaultLoadManager();
			syncer1 = new DefaultSyncer(config1, volume1, manager1);
			syncer2 = new DefaultSyncer(config2, volume2, manager2);

			syncer1.setPollInterval(1, TimeUnit.MILLISECONDS);
			syncer1.run();
			syncer1.waitFor();
			managerThread1 = new Thread(manager1);
			managerThread1.start();

			syncer2.setPollInterval(1, TimeUnit.MILLISECONDS);
			syncer2.run();
			syncer2.waitFor();
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

	protected static void waitUntil(Callable<Boolean> evaluate) {
		long timeout = 1000L;
		waitUntil(evaluate, timeout);
	}

	protected static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		try {
			long startTime = System.currentTimeMillis();
			while (!evaluate.call()) {
				Thread.yield();
				if (System.currentTimeMillis() - timeout > startTime) {
					fail("wait timeout");
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void syncsFileInSubdirectory() throws Exception {
		Path dir1 = Paths.get(tmpDir1.toString(), "dir");
		Files.createDirectories(dir1);

		Path dir2 = Paths.get(tmpDir2.toString(), "dir");
		final SyncIntegrationTest test = this;
		waitUntil(() -> {
			final SyncIntegrationTest test2 = test;
			return Files.isDirectory(dir2);
		}, 1000000L);

		Path file1 = Paths.get(dir1.toString(), "file");
		Files.write(file1, "text".getBytes());

		Path file2 = Paths.get(dir2.toString(), "file");
		waitUntil(() -> Files.exists(file2), 1000000L);
		assertEquals("text", new String(Files.readAllBytes(file2)));
	}
}
