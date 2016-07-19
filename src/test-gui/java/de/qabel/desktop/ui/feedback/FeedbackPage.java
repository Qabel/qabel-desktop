package de.qabel.desktop.ui.feedback;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.testfx.api.FxRobot;

public class FeedbackPage extends AbstractPage {
    private FeedbackController controller;

    public FeedbackPage(FXRobot baseFXRobot, FxRobot robot, FeedbackController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }


    public void sendFeedback() {
        typeFeedbackInformations();
        clickOn(controller.submitButton);
        confirmAlertBox();
    }

    private void confirmAlertBox() {
        DialogPane alertDialog = controller.alert.getDialogPane();
        clickOn(alertDialog.lookupButton(ButtonType.OK));
    }


    private void typeFeedbackInformations() {
        controller.infoMessage = "guitest infoMessage";
        controller.titleBar = "guitest titleBar";

        clickOn(controller.nameField).write("guitest nameField");
        clickOn(controller.emailField).write("guitest emailField");
        clickOn(controller.feedbackField).write("guitest feedbackField");

    }
}
