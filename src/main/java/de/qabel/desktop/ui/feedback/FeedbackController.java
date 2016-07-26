package de.qabel.desktop.ui.feedback;


import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javax.inject.Inject;
import java.io.IOException;
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
    private ResourceBundle resourceBundle;
    String infoMessage;
    String infoTitle;

    @FXML
    Button submitButton;

    Alert alert;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        infoMessage = resourceBundle.getString("feedBackInfoMessage");
        infoTitle = resourceBundle.getString("feedBackInfoHeader");
    }

    @FXML
    void handleSendButtonAction() {

        new Thread() {
            @Override
            public void run() {
                try {
                    reportHandler.sendFeedback(feedbackField.getText(), nameField.getText(), emailField.getText());
                    Platform.runLater(() -> {
                        showThanksDialog();
                        feedbackField.setText("");
                        nameField.setText("");
                        emailField.setText("");
                    });
                } catch (IOException e) {
                    alert(e);
                }
            }

        }.start();
    }

    void showThanksDialog() {
        alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(infoTitle);
        alert.setContentText(infoMessage);
        alert.show();
    }

}
