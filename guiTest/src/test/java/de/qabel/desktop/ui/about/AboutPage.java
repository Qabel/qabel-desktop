package de.qabel.desktop.ui.about;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class AboutPage extends AbstractPage {
    private AboutController controller;
    private static final String ROOT_NODE = "#aboutViewRoot";

    public AboutPage(FXRobot baseFXRobot, FxRobot robot, AboutController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void showPopup() {
        clickOn("#thanksButton");
        waitUntil(controller.popupController.aboutPopup::isVisible);
    }

    public boolean waitForRootNode() {
        return waitForNode(ROOT_NODE) != null;
    }
}
