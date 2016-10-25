package de.qabel.desktop.ui.accounting;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class AccountingPage extends AbstractPage {
    private AccountingController controller;

    public AccountingPage(FXRobot baseFXRobot, FxRobot robot, AccountingController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void open() {
        clickOn("#addIdentity");
        waitUntil(() -> controller.wizardController != null);
        waitUntil(() -> controller.wizardController.wizardPane != null);
    }
}
