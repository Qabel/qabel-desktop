package de.qabel.desktop.ui.accounting.identity;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

class IdentityEditViewPage extends AbstractPage {

    private IdentityEditController controller;

    private static final String ALIAS_FIELD = "#alias";
    private static final String EMAIL_FIELD = "#email";
    private static final String PHONE_FIELD = "#phone";
    private static final String UPDATE_BUTTON = "#saveIdentity";

    IdentityEditViewPage(FXRobot baseFXRobot, FxRobot robot, IdentityEditController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    void enterAlias(String s) {
        clickOn(ALIAS_FIELD).write(s);
    }

    void enterEmail(String s) {
        clickOn(EMAIL_FIELD).write(s);
    }

    void enterPhone(String s) {
        clickOn(PHONE_FIELD).write(s);
    }

    void presSave() {
        clickOn(UPDATE_BUTTON);
    }

}
