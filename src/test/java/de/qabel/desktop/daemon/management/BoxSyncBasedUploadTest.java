package de.qabel.desktop.daemon.management;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.event.WatchRegisteredEvent;
import de.qabel.desktop.storage.BoxVolumeConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.*;

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

		IdentityBuilderFactory identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
		Identity identity = identityBuilderFactory.factory().build();
		Account account = new Account("a", "b", "c");
		MagicEvilPrefixSource.set(account, "custom prefix");

		BoxSyncConfig boxSyncConfig = new DefaultBoxSyncConfig(tmpDir, Paths.get("/tmp"), identity, account);
		WatchEvent event = new WatchRegisteredEvent(file.toPath());
		Upload upload = new BoxSyncBasedUpload(boxSyncConfig, event);

		BoxVolumeConfig boxVolumeConfig = upload.getBoxVolumeConfig();
		assertEquals(identity.getPrimaryKeyPair(), boxVolumeConfig.getKeyPair());
		assertEquals(new File(System.getProperty("java.io.tmpdir")), boxVolumeConfig.getTmpDir());
		assertEquals(BoxVolumeConfig.THE_ONE_AND_ONLY_BUCKET_NAME, boxVolumeConfig.getBucket());
		assertEquals("custom prefix", boxVolumeConfig.getPrefix());
		assertEquals(new File(tmpDir.toFile(), "testfile").toPath(), upload.getSource());
		assertEquals(Paths.get("/tmp/testfile"), upload.getDestination());
	}
}