package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.ButtonType;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.assertTrue;

public class AccountingItemGuiTest extends AbstractGuiTest<AccountingItemController> {

	private Identity identity;

	@Override
	protected FXMLView getView() {
		identity = new Identity("alias", null, null);
		return new AccountingItemView(generateInjection("identity", identity));
	}

	@Test
	public void testEdit() throws Exception {
		clickOn("#edit");
		waitUntil(() -> controller.dialog != null);
		runLaterAndWait(() -> {
			controller.dialog.getEditor().setText("new alias");
			robot.clickOn(controller.dialog.getDialogPane().lookupButton(ButtonType.OK));
		});
		waitUntil(() -> identity.getAlias().equals("new alias"));
	}

	@Test
	public void selectsIdentity() {
		clickOn("#selectedRadio");
		waitUntil(() -> identity.equals(clientConfiguration.getSelectedIdentity()));
		assertTrue(controller.selectedRadio.isSelected());
	}
}
