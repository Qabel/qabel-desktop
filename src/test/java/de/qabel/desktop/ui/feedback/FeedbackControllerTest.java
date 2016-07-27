package de.qabel.desktop.ui.feedback;

import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.util.UTF8Converter;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class FeedbackControllerTest extends AbstractControllerTest {
    private FeedbackController controller;
    private ResourceBundle resourceBundle;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        resourceBundle = ResourceBundle.getBundle("ui", new Locale("te", "ST"), new UTF8Converter());
    }

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
    public void showAlertDialog() {
        createController();
        runLaterAndWait(() -> {
            controller.showThanksDialog();
        });
        String alertInfoTitle = resourceBundle.getString("feedBackInfoHeader");
        String alertInfoMessage = resourceBundle.getString("feedBackInfoMessage");

        assertNotNull(controller.alert);
        assertEquals(alertInfoTitle, controller.alert.getTitle());
        assertEquals(alertInfoMessage, controller.alert.getContentText());
        assertAsync(controller.alert::isShowing);
    }

    private void createController() {
        Locale.setDefault(new Locale("te", "ST"));
        FeedbackView view = new FeedbackView();
        controller = (FeedbackController) view.getPresenter();
    }

}
