package de.qabel.desktop.ui.accounting.login;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class LoginPage extends AbstractPage {
    public static final String CREATE_BUTTON = "#createButton";
    public static final String CONFIRM_PASSWORD_INPUT = "#confirm";
    public static final String PASSWORD_INPUT = "#password";
    public static final String EMAIL_INPUT = "#email";
    public static final String LOGIN_BUTTON = "#loginButton";
    public static final String USER_INPUT = "#user";
    private LoginController controller;

    public LoginPage(FXRobot baseFXRobot, FxRobot robot, LoginController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void clear() {
        runLaterAndWait(() -> controller.user.clear());
        runLaterAndWait(() -> controller.password.clear());
    }

    public void setServer(String url) {
        runLaterAndWait(() -> {
            controller.providerChoices.getItems().clear();
            controller.providerChoices.getItems().add(url);
            controller.providerChoices.setValue(url);
        });
    }

    public LoginPage loginAndExpectError() {
        login();
        waitForLoginError();
        return this;
    }

    public LoginPage waitForLoginError() {
        waitUntil(() -> controller.loginButton.getStyleClass().contains("error"));
        return this;
    }

    public LoginPage waitForCreateError() {
        waitUntil(() -> controller.createButton.getStyleClass().contains("error"), 5000L);
        return this;
    }

    public LoginPage login() {
        clickOn(LOGIN_BUTTON);
        return this;
    }

    public LoginPage createAccount(String username, String email, String password) {
        return createAccount(username, email, password, password);
    }

    public LoginPage createAccount(String username, String email, String password, String passwordConfirmation) {
        withCreateAccount();
        waitForNode(USER_INPUT);
        waitForNode(EMAIL_INPUT);
        waitForNode(PASSWORD_INPUT);
        waitForNode(CONFIRM_PASSWORD_INPUT);

        runLaterAndWait(controller.password::clear);
        runLaterAndWait(controller.user::clear);

        setUsername(username);
        setEmail(email);
        setPassword(password);
        setPasswordConfirmation(passwordConfirmation);
        create();
        return this;
    }

    public LoginPage withCreateAccount() {
        clickOn("#openCreateButton");
        return this;
    }

    public LoginPage create() {
        clickOn(CREATE_BUTTON);
        return this;
    }

    /**
     * Set username via controller (no gui input)
     */
    public LoginPage setUsername(String username) {
        controller.user.setText(username);
        return this;
    }

    /**
     * Set username via gui (click & type)
     */
    public LoginPage enterUsername(String username) {
        clickOn(USER_INPUT);
        runLaterAndWait(() -> controller.user.clear());
        clickOn(USER_INPUT).write(username);
        return this;
    }

    /**
     * Set password confirmation via controller (no gui input)
     */
    public LoginPage setPasswordConfirmation(String password) {
        controller.confirm.setText(password);
        return this;
    }

    /**
     * Set password confirmation via GUI (click & type)
     */
    public LoginPage enterPasswordConfirmation(String password) {
        clickOn(CONFIRM_PASSWORD_INPUT).write(password);
        return this;
    }

    /**
     * Set password via controller (not gui input)
     */
    public LoginPage setPassword(String password) {
        controller.password.setText(password);
        return this;
    }

    /**
     * Set password via GUI (click & type)
     */
    public LoginPage enterPassword(String password) {
        clickOn(PASSWORD_INPUT).write(password);
        return this;
    }

    /**
     * Set email via controller (no gui input)
     */
    public LoginPage setEmail(String email) {
        controller.email.setText(email);
        return this;
    }

    /**
     * Set email via GUI (click & type)
     */
    public LoginPage enterEmail(String email) {
        clickOn(EMAIL_INPUT).write(email);
        return this;
    }
}
