package de.qabel.desktop.ui.accounting.login;

import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertTrue;


public class LoginGuiTest extends AbstractGuiTest<LoginController> {
    private LoginPage page;

    @Override
    protected LoginView getView() {
        return new LoginView();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new LoginPage(baseFXRobot, robot, controller);
    }

    private void setup() {
        page.clear();
        page.setServer("http://localhost:9696");
    }

    @Test
    public void showsFailureOnInvalidCredentials() {
        setup();
        page.enterUsername("invalid user").loginAndExpectError();
    }

    private Random random = new Random();

    @Test
    public void correctCreateBoxAccount() {
        setup();
        int i = random.nextInt(100000);
        String name = "validUserName" + i;
        String email = "correctmail" + i + "@example.de";
        page.createAccount(name, email, "123456");

        assertTrue(!controller.map.containsKey("email"));
        assertTrue(!controller.map.containsKey("password"));
        assertTrue(!controller.map.containsKey("username"));
    }

    @Test
    public void correctCreateBoxAccountBinding() {
        setup();
        int i = random.nextInt(100000);
        String name = "validUserName" + i;
        String email = "correctmail" + i + "@example.de";
        page.withCreateAccount()
            .enterUsername(name)
            .enterEmail(email)
            .enterPassword("123456")
            .create();

        assertTrue(!controller.map.containsKey("email"));
        assertTrue(!controller.map.containsKey("password"));
        assertTrue(!controller.map.containsKey("username"));
    }

    @Test
    public void usernameFail() {
        setup();
        page.createAccount("invalid User", "correctmail@example.de", "123456");
        expectErrorOn("username");
    }

    public void expectErrorOn(String field) {
        waitUntil(() -> controller.map.containsKey(field), 5000L);
    }

    @Test
    public void emailFail() {
        setup();
        page.createAccount("validUserName", "111", "123456");
        expectErrorOn("email");
    }

    @Test
    public void passwordFail() {
        setup();
        page.createAccount("validUserName", "correctmail@example.de", "111");
        expectErrorOn("password1");
    }

    @Test
    public void passwordsNotEqualFail() {
        setup();

        page.createAccount("validUserName", "correctmail@example.de", "111111", "222222");
        expectErrorOn("password1");
    }

    @Test
    public void errorButtonTest() {
        setup();

        page.withCreateAccount().create()
            .waitForCreateError();
    }
}
