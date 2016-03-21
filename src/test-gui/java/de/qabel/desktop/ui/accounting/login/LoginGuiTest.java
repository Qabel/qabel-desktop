package de.qabel.desktop.ui.accounting.login;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertTrue;


public class LoginGuiTest extends AbstractGuiTest<LoginController> {
	@Override
	protected LoginView getView() {
		return new LoginView();
	}

	private void setup() {
		runLaterAndWait(() -> {
			controller.providerChoices.getItems().clear();
			controller.providerChoices.getItems().add("http://localhost:9696");
			controller.providerChoices.setValue("http://localhost:9696");
		});
		runLaterAndWait(() -> controller.user.clear());
		runLaterAndWait(() -> controller.password.clear());
	}

	@Test
	public void showsFailureOnInvalidCredentials() {
		setup();
		clickOn("#user");
		runLaterAndWait(() -> controller.user.clear());
		clickOn("#user").write("invalid user");
		clickOn("#loginButton");
		waitUntil(() -> controller.loginButton.getStyleClass().contains("error"));
	}

	private Random random = new Random();

	@Test
	public void correctCreateBoxAccount() {
		clickOn("#openCreateButton");
		int i = random.nextInt(100000);
		setup();
		String name = "validUserName" + i;
		String email = "correctmail" + i + "@example.de";
		clickOn("#user").write(name);
		clickOn("#email").write(email);
		clickOn("#password").write("123456");
		clickOn("#confirm").write("123456");
		clickOn("#createButton");
		assertTrue(!controller.map.containsKey("email"));
		assertTrue(!controller.map.containsKey("password"));
		assertTrue(!controller.map.containsKey("username"));
	}

	@Test
	public void usernameFail() {
		clickOn("#openCreateButton");

		setup();
		String name = "invalid User";
		String email = "correctmail@example.de";
		clickOn("#user").write(name);
		clickOn("#email").write(email);
		clickOn("#password").write("123456");
		clickOn("#confirm").write("123456");
		clickOn("#createButton");
		waitUntil(() -> controller.map.containsKey("username"), 5000L);
	}

	@Test
	public void emailFail() {
		clickOn("#openCreateButton");

		setup();
		String name = "validUserName";
		String email = "111";
		clickOn("#user").write(name);
		clickOn("#email").write(email);
		clickOn("#password").write("123456");
		clickOn("#confirm").write("123456");
		clickOn("#createButton");
		waitUntil(() -> controller.map.containsKey("email"), 5000L);
	}

	@Test
	public void passwordFail() {
		clickOn("#openCreateButton");
		setup();
		String name = "validUserName";
		String email = "correctmail@example.de";
		clickOn("#user").write(name);
		clickOn("#email").write(email);
		clickOn("#password").write("111");
		clickOn("#confirm").write("111");
		clickOn("#createButton");
		waitUntil(() -> controller.map.containsKey("password1"), 5000L);
	}

	@Test
	public void passwordsNotEqualFail() {
		clickOn("#openCreateButton");

		runLaterAndWait(controller.password::clear);
		runLaterAndWait(controller.user::clear);

		setup();
		String name = "validUserName";
		String email = "correctmail@example.de";
		clickOn("#user").write(name);
		clickOn("#email").write(email);

		clickOn("#password").write("111111");
		clickOn("#confirm").write("222222");
		clickOn("#createButton");
		waitUntil(() -> controller.map.containsKey("password1"), 5000L);
	}

	@Test
	public void errorButtonTest() {
		setup();
		runLaterAndWait(() -> controller.user.clear());
		clickOn("#openCreateButton");
		clickOn("#createButton");
		waitUntil(() -> controller.createButton.getStyleClass().contains("error"), 5000L);
		assertTrue(controller.createButton.getStyleClass().contains("error"));
	}
}
