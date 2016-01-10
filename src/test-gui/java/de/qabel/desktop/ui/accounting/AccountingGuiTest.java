package de.qabel.desktop.ui.accounting;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.ButtonType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountingGuiTest extends AbstractGuiTest<AccountingController> {
	@Override
	protected FXMLView getView() {
		return new AccountingView();
	}

	@Test
	public void testAddsIdentity() throws EntityNotFoundExcepion {
		robot.clickOn("#add");
		waitUntil(() -> controller.dialog != null);
		runLaterAndWait(() -> controller.dialog.getEditor().setText("a new identity"));
		robot.clickOn(controller.dialog.getDialogPane().lookupButton(ButtonType.OK));

		assertEquals(1, identityRepository.findAll().size());
		assertEquals("a new identity", identityRepository.findAll().get(0).getAlias());
	}
}
