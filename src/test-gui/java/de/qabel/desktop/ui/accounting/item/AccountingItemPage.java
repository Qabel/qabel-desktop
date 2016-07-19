package de.qabel.desktop.ui.accounting.item;

import com.sun.javafx.robot.FXRobot;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

import static org.junit.Assert.assertEquals;

public class AccountingItemPage extends AbstractPage {
    private AccountingItemController controller;

    public AccountingItemPage(FXRobot baseFXRobot, FxRobot robot, AccountingItemController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void select() {
        if (controller.selectedRadio.isSelected()) {
            clickOn("#selectedRadio");
            waitUntil(() -> !controller.selectedRadio.isSelected());
        }
        clickOn("#selectedRadio");
        waitUntil(controller.selectedRadio::isSelected);
    }

    public void openMenu() {
        clickOn("#menu");
    }

    public void edit() {
        clickOn("#menu");
        controller.identityMenuController.setAlias("new alias identity");
        assertEquals("new alias identity", controller.getIdentity().getAlias());
    }
}
