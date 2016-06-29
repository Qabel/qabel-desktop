package de.qabel.desktop.ui.accounting.item;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.InputDialogPage;
import org.testfx.api.FxRobot;

public class AccountingItemPage extends AbstractPage {
    private AccountingItemController controller;

    public AccountingItemPage(FXRobot baseFXRobot, FxRobot robot, AccountingItemController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public InputDialogPage edit() {
        controller.dialog = null;
        clickOn("#edit");
        waitUntil(() -> controller.dialog != null);
        return new InputDialogPage(baseFXRobot, robot, controller.dialog);
    }

    public void select() {
        if (controller.selectedRadio.isSelected()) {
            clickOn("#selectedRadio");
            waitUntil(() -> !controller.selectedRadio.isSelected());
        }
        clickOn("#selectedRadio");
        waitUntil(controller.selectedRadio::isSelected);
    }

    public void openMenu(){
        clickOn("#edit");
    }
}
