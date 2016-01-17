package de.qabel.desktop.daemon.sync.worker;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.management.DefaultLoadManager;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DefaultSyncerTest extends AbstractSyncTest {
	private LoadManager manager;
	private BoxSyncConfig config;
	private Identity identity;
	private Account account;

	@Before
	public void setUp() {
		super.setUp();
		try {
			identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000")).factory().build();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		account = new Account("a", "b", "c");
		manager = new DefaultLoadManager();
		config = new DefaultBoxSyncConfig(tmpDir, Paths.get("/tmp"), identity, account);
	}

	@After
	public void tearDown() throws InterruptedException {
		super.tearDown();
	}

	@Test
	public void addsFilesAsUploads() throws IOException {
		new File(tmpDir.toFile(), "file").createNewFile();

		Syncer syncer = new DefaultSyncer(config, manager);
		syncer.run();

		waitUntil(() -> manager.getUploads().size() == 2);
	}
}
