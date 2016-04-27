package de.qabel.desktop.ui.contact;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.contact.item.ContactItemController;
import org.testfx.api.FxRobot;

public class ContactPage extends AbstractPage {
    private ContactController controller;

    public ContactPage(FXRobot baseFXRobot, FxRobot robot, ContactController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public ContactItemPage getFirstItem() {
        waitUntil(() -> controller.contactItems.size() == 1);
        ContactItemController itemController = controller.contactItems.get(0);
        return new ContactItemPage(baseFXRobot, robot, itemController);
    }

    public void selectFirstItem() {
        clickOn(".contactItem #alias");
    }
}
