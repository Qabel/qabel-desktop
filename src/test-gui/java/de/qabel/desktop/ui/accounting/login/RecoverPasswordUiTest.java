package de.qabel.desktop.ui.accounting.login;

import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static junit.framework.Assert.assertTrue;


public class RecoverPasswordUiTest extends AbstractGuiTest<LoginController> {
	@Override
	protected LoginView getView() {
		return new LoginView();
	}

	@Test
	public void incorrectEMail() {
		clickOn("#recoverPassword");
		clickOn("#newPassword");
		waitUntil(() -> controller.newPassword.getStyleClass().contains("error"), 5000L);
	}

	@Test
	public void EMailSendCorrect() {
		clickOn("#recoverPassword");
		clickOn("#email").write("valid.mail@example.com");
		clickOn("#newPassword");
		waitUntil(controller.newPassword::isDisabled);
	}
}
