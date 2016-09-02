package de.qabel.desktop.ui.contact;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.contact.item.ContactItemController;
import javafx.scene.Node;
import org.testfx.api.FxRobot;

public class ContactItemPage extends AbstractPage {
    private ContactItemController controller;

    public ContactItemPage(FXRobot baseFXRobot, FxRobot robot, ContactItemController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public Integer getIndicatorCount() {
        String indicatorText = controller.getIndicator().getText();
        return indicatorText.isEmpty() ? null : Integer.parseInt(indicatorText);
    }

    public String getAvatarStyle() {
        return getFirstNode("#avatar").getStyle();
    }

    public Node getDeleteButton() {
        return getFirstNode("#delete");
    }

    public void delete() {
        clickOn("#delete");
    }
}
