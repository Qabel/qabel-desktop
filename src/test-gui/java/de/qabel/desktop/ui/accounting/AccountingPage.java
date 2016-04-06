package de.qabel.desktop.ui.accounting;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.InputDialogPage;
import org.testfx.api.FxRobot;

public class AccountingPage extends AbstractPage {
    private AccountingController controller;

    public AccountingPage(FXRobot baseFXRobot, FxRobot robot, AccountingController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public InputDialogPage add() {
        controller.dialog = null;
        clickOn("#add");
        waitUntil(() -> controller.dialog != null);
        return new InputDialogPage(baseFXRobot, robot, controller.dialog);
    }
}
