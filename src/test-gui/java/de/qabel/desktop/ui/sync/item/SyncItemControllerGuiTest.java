package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.sync.worker.FakeSyncer;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyncItemControllerGuiTest extends AbstractGuiTest<SyncItemController> {
	private Identity identity;
	private Account account;
	private BoxSyncConfig syncConfig;
	private FakeSyncer syncer;
	private SyncItemPage page;

	@Override
    @Before
	public void setUp() throws Exception {
		identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
		identity = identityBuilderFactory.factory().build();
		account = new Account("a", "b", "c");
		syncConfig = new DefaultBoxSyncConfig("testsync", Paths.get("tmp"), Paths.get("tmp"), identity, account);
		syncer = new FakeSyncer(syncConfig);
		syncConfig.setSyncer(syncer);
		super.setUp();
		clientConfiguration.getBoxSyncConfigs().clear();
		clientConfiguration.getBoxSyncConfigs().add(syncConfig);
		page = new SyncItemPage(baseFXRobot, robot, controller);
	}

	@Override
	protected FXMLView getView() {
		return new SyncItemView(s -> s.equals("syncConfig") ? syncConfig : null);
	}

	@Test
	public void showsItemsProperties() {
		assertEquals("testsync", page.name());
		assertEquals(Paths.get("tmp").toAbsolutePath().toString(), page.localPath());
		assertEquals("/tmp", page.remotePath());
	}

	@Test
	public void refreshesOnConfigChange() {
		runLaterAndWait(() -> {
			syncConfig.setName("changed");
			syncConfig.setLocalPath(Paths.get("to something"));
			syncConfig.setRemotePath(Paths.get("else"));
		});

		assertEquals("changed", page.name());
		assertEquals(Paths.get("to something").toAbsolutePath().toString(), page.localPath());
		assertEquals("/else", page.remotePath());
	}

	@Test
	public void deletesSyncCleanly() {
		page.delete().yes();
		waitUntil(syncer::isStopped);
		assertTrue(clientConfiguration.getBoxSyncConfigs().isEmpty());
	}

	@Test
	public void doesNotDeleteSyncIfUserIsUnsure() {
		page.delete().cancel();
		waitUntil(() -> controller.confirmationDialog == null);
		assertFalse(syncer.isStopped());
		assertFalse(clientConfiguration.getBoxSyncConfigs().isEmpty());
	}
}
