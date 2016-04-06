package de.qabel.desktop.ui.accounting.login;

import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static junit.framework.Assert.assertTrue;


public class RecoverPasswordUiTest extends AbstractGuiTest<LoginController> {
    private LoginPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new LoginPage(baseFXRobot, robot, controller);
    }

    @Override
    protected LoginView getView() {
        return new LoginView();
    }

    @Test
    public void incorrectEMail() {
        page.expandRecoverPassword()
            .requestNewPassword();
        waitUntil(() -> controller.newPassword.getStyleClass().contains("error"));
    }

    @Test
    public void EMailSendCorrect() {
        page.expandRecoverPassword()
            .enterEmail("valid.mail@example.com")
            .requestNewPassword();

        waitUntil(controller.newPassword::isDisabled);
    }
}
