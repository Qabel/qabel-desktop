package de.qabel.desktop.ui.accounting;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
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
	public void testAddsIdentity() throws EntityNotFoundExcepion, PersistenceException {
		clickOn("#add");
		waitUntil(() -> controller.dialog != null);
		runLaterAndWait(() -> controller.dialog.getEditor().setText("a new identity"));
		controller.clientConfiguration.selectIdentity(null);

		clickOn(controller.dialog.getDialogPane().lookupButton(ButtonType.OK));

		Identities identities = identityRepository.findAll();
		assertEquals(1, identities.getIdentities().size());
		Identity i = controller.clientConfiguration.getSelectedIdentity();
		assertEquals("a new identity", identities.getByKeyIdentifier(i.getKeyIdentifier()).getAlias());
	}
}
