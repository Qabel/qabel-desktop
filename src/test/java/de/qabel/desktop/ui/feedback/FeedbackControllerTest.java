package de.qabel.desktop.ui.feedback;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class FeedbackControllerTest extends AbstractControllerTest {
    FeedbackController controller;

    private static final String ALERT_MESSAGE = "Thank you for your feedback";
    private static final String ALERT_TITLE = "Send feedback was successful";

    @Test
    public void createEMailBodyTest() {
        createController();
        controller.nameField.setText("name");
        controller.emailField.setText("email");
        controller.feedbackField.setText("feedback");

        controller.handleSendButtonAction();

        assertAsync(controller.nameField::getText, equalTo(""));
        assertAsync(controller.emailField::getText, equalTo(""));
        assertAsync(controller.feedbackField::getText, equalTo(""));
    }

    @Test
    public void createAlertInformation() {
        createController();
        assertEquals(ALERT_MESSAGE, controller.infoMessage);
        assertEquals(ALERT_TITLE, controller.titleBar);
    }

    @Test
    public void showAlertDialog() {
        createController();
        runLaterAndWait(() -> {
            controller.showThanksDialog();
        });

        assertNotNull(controller.alert);
        assertEquals(ALERT_TITLE, controller.alert.getTitle());
        assertEquals(ALERT_MESSAGE, controller.alert.getContentText());
        assertAsync(controller.alert::isShowing);
    }

    private void createController() {
        Locale.setDefault(new Locale("te", "ST"));
        FeedbackView view = new FeedbackView();
        controller = (FeedbackController) view.getPresenter();
    }

}
