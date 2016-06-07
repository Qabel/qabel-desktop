package de.qabel.desktop.ui.about;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

import static org.junit.Assert.assertTrue;

public class AboutPage extends AbstractPage {
    private AboutController controller;

    public AboutPage(FXRobot baseFXRobot, FxRobot robot, AboutController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void btnShowPopup() {
        clickOn("#btnThanks");
        setPopupText();
        showAboutPopup();
        waitUntil(() -> controller.popupController.textAreaPopup != null);
        assertTrue(controller.popupController.aboutPopup.isVisible());
    }

    private void setPopupText() {
        controller.popupController.setTextAreaContent("test thanks file");
    }

    private void showAboutPopup() {
        controller.popupController.showPopup();
    }
}

