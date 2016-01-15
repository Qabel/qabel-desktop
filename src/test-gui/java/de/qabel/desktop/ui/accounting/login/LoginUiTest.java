package de.qabel.desktop.ui.accounting.login;

import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;


public class LoginUiTest extends AbstractGuiTest<LoginController> {
	@Override
	protected LoginView getView() {
		return new LoginView();
	}

	@Test
	public void showsFailureOnInvalidCredentials() {
		runLaterAndWait(() -> controller.user.clear());
		clickOn("#user").write("invalid user");
		clickOn("#loginButton");
		waitUntil(() -> controller.loginButton.getStyleClass().contains("error"), 5000L);
	}
}
