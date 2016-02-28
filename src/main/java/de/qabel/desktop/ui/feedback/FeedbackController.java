package de.qabel.desktop.ui.feedback;


import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class FeedbackController extends AbstractController implements Initializable {


	@FXML
	TextArea feedbackField;

	@FXML
	TextField nameField;

	@FXML
	TextField emailField;

	@Inject
	private CrashReportHandler reportHandler;

	public void initialize(URL location, ResourceBundle resources) {

	}

	@FXML
	protected void handleSendButtonAction(ActionEvent event) {
		handleSendButtonAction();
	}

	protected void handleSendButtonAction() {

		new Thread() {
			public void run() {
				try {
					reportHandler.sendFeedback(feedbackField.getText(), nameField.getText(), emailField.getText());

					feedbackField.setText("");
					nameField.setText("");
					emailField.setText("");
				} catch (IOException  e) {
					alert(e);
				}
			}
		}.start();




	}
}


