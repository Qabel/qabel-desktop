package de.qabel.desktop.daemon.sync.worker;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.Download;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class DefaultSyncerTest extends AbstractSyncTest {
	private TransferManager manager;
	private BoxSyncConfig config;
	private Identity identity;
	private Account account;
	private DefaultSyncer syncer;

	@Before
	public void setUp() {
		super.setUp();
		try {
			identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000")).factory().build();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		account = new Account("a", "b", "c");
		manager = new DefaultTransferManager();
		config = new DefaultBoxSyncConfig(tmpDir, Paths.get("/tmp"), identity, account);
	}

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
	}

	@Test(expected = IllegalStateException.class)
	public void bootstrappingFailsIfLocalDirDoesNotExist() throws IOException {
		FileUtils.deleteDirectory(tmpDir.toFile());

		syncer = new DefaultSyncer(config, new BoxVolumeStub(), manager);
		syncer.run();
	}
}
