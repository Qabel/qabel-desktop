package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SyncItemGuiTest extends AbstractGuiTest<SyncItemController> {
	private Identity identity = new Identity("some alias", null, null);
	private Account account = new Account("provider", "user", "auth");
	private final BoxSyncConfig config = new DefaultBoxSyncConfig(Paths.get("1"), Paths.get("2"), identity, account);

	@Override
	protected FXMLView getView() {
		return new SyncItemView((s) -> config);
	}

	@Test
	public void setsProperties() {
	}
}
