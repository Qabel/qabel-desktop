package de.qabel.desktop.daemon.management;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.event.WatchRegisteredEvent;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.desktop.storage.BoxVolume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class BoxSyncBasedUploadTest extends AbstractSyncTest {
	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() throws InterruptedException {
		super.tearDown();
	}

	@Test
	public void forwardsConfig() throws Exception {
		File file = new File(tmpDir.toFile(), "testfile");
		file.createNewFile();

		IdentityBuilderFactory identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
		Identity identity = identityBuilderFactory.factory().build();
		Account account = new Account("a", "b", "c");
		MagicEvilPrefixSource.set(account, "custom prefix");

		BoxSyncConfig boxSyncConfig = new DefaultBoxSyncConfig(tmpDir, Paths.get("/tmp"), identity, account);
		WatchEvent event = new WatchRegisteredEvent(file.toPath());
		BoxVolumeStub volume = new BoxVolumeStub();
		Upload upload = new BoxSyncBasedUpload(volume, boxSyncConfig,event);

		BoxVolume boxVolume = upload.getBoxVolume();
		assertEquals(volume, boxVolume);
		assertEquals(new File(tmpDir.toFile(), "testfile").toPath(), upload.getSource());
		assertEquals(Paths.get("/tmp/testfile"), upload.getDestination());
	}
}