package de.qabel.desktop.ui.accounting.identity;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

class IdentityEditViewPage extends AbstractPage {

    private IdentityEditController controller;

    private static final String ALIAS_FIELD = "#aliasField";
    private static final String EMAIL_FIELD = "#emailField";
    private static final String PHONE_FIELD = "#phoneField";
    private static final String UPDATE_BUTTON = "#updateIdentity";

    IdentityEditViewPage(FXRobot baseFXRobot, FxRobot robot, IdentityEditController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    void setAlias(String s) {
        waitForNode(ALIAS_FIELD);
        clickOn(ALIAS_FIELD).write(s);
    }

    void setEmail(String s) {
        waitForNode(EMAIL_FIELD);
        clickOn(EMAIL_FIELD).write(s);
    }

    void setPhone(String s) {
        waitForNode(PHONE_FIELD);
        clickOn(PHONE_FIELD).write(s);
    }

    void updateIdentity() {
        clickOn(UPDATE_BUTTON);
    }

}
