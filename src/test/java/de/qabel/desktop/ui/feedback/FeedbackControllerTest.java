package de.qabel.desktop.ui.feedback;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class FeedbackControllerTest extends AbstractControllerTest {
    FeedbackController controller;

    String infoMessage = "infoMessage";
    String titleBar = "titleBar";

    @Test
    public void createEMailBodyTest() {
        createController();
        controller.nameField.setText("name");
        controller.emailField.setText("email");
        controller.feedbackField.setText("feedback");

        controller.handleSendButtonAction();
        waitUntil(() -> controller.nameField.getText().equals(""));
        waitUntil(() -> controller.emailField.getText().equals(""));
        waitUntil(() -> controller.feedbackField.getText().equals(""));
    }

    @Test
    public void createAltertInformations() {
        createController();

        assertEquals(titleBar, controller.titleBar);
        assertEquals(infoMessage, controller.infoMessage);
    }

    private void createController() {
        Locale.setDefault(new Locale("te", "ST"));
        FeedbackView view = new FeedbackView();
        controller = (FeedbackController) view.getPresenter();
        controller.titleBar = titleBar;
        controller.infoMessage = infoMessage;
    }


}
