package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.Labeled;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SyncItemControllerGuiTest extends AbstractGuiTest<SyncItemController> {
	private Identity identity;
	private Account account;
	private BoxSyncConfig syncConfig;

	@Before
	public void setUp() throws Exception {
		identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
		identity = identityBuilderFactory.factory().build();
		account = new Account("a", "b", "c");
		syncConfig = new DefaultBoxSyncConfig("testsync", Paths.get("tmp"), Paths.get("tmp"), identity, account);
		super.setUp();
	}

	@Override
	protected FXMLView getView() {
		return new SyncItemView(s -> syncConfig);
	}

	@Test
	public void showsItemsProperties() {
		assertEquals("testsync", name().getText());
		assertEquals(Paths.get("tmp").toAbsolutePath().toString(), localPath().getText());
		assertEquals("/tmp", remotePath().getText());
	}

	@Test
	public void refreshesOnConfigChange() {
		runLaterAndWait(() -> {
			syncConfig.setName("changed");
			syncConfig.setLocalPath(Paths.get("to something"));
			syncConfig.setRemotePath(Paths.get("else"));
		});

		assertEquals("changed", name().getText());
		assertEquals(Paths.get("to something").toAbsolutePath().toString(), localPath().getText());
		assertEquals("/else", remotePath().getText());
	}

	private Labeled remotePath() {
		return (Labeled)getFirstNode("#remotePath");
	}

	private Labeled localPath() {
		return (Labeled)getFirstNode("#localPath");
	}

	public Labeled name() {
		return (Labeled) getFirstNode("#name");
	}
}
