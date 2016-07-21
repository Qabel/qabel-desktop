package de.qabel.desktop.ui.feedback;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.testfx.api.FxRobot;

class FeedbackPage extends AbstractPage {
    private FeedbackController controller;

    FeedbackPage(FXRobot baseFXRobot, FxRobot robot, FeedbackController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    void sendFeedback() {
        typeFeedbackInformations();
        clickOn(controller.submitButton);
    }

    void confirmAlertBox() {
        Node button = getDialogPane().lookupButton(ButtonType.OK);
        clickOn(button);
    }

    DialogPane getDialogPane() {
        return controller.alert.getDialogPane();
    }


    private void typeFeedbackInformations() {
        controller.infoMessage = "guitest infoMessage";
        controller.titleBar = "guitest titleBar";

        clickOn(controller.nameField).write("guitest nameField");
        clickOn(controller.emailField).write("guitest emailField");
        clickOn(controller.feedbackField).write("guitest feedbackField");

    }
}
