package de.qabel.desktop.ui.feedback;

import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.invite.InviteController;
import de.qabel.desktop.ui.invite.InviteView;
import org.junit.Test;

import java.util.Locale;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.assertEquals;


public class FeedbackControllerTest extends AbstractControllerTest {
	FeedbackController controller;

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


	private void createController() {
		Locale.setDefault(new Locale("te", "ST"));
		FeedbackView view = new FeedbackView();
		controller = (FeedbackController) view.getPresenter();
	}

}
