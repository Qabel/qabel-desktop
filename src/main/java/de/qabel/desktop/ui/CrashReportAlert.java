package de.qabel.desktop.ui;

import de.qabel.desktop.crashReports.CrashReportHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashReportAlert {
    private final Logger logger = LoggerFactory.getLogger(CrashReportAlert.class);
    private final CrashReportHandler reportHandler;

    private Alert alert;
    private Label exceptionLabel;
    private TextArea inputArea;

    public CrashReportAlert(CrashReportHandler reportHandler, String message, Exception e) {
        this.reportHandler = reportHandler;

        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        Label feedbackLabel = new Label("Feedback");
        Label stackTraceLabel = new Label("Stack Trace");

        inputArea = new TextArea();
        inputArea.getStyleClass().add("feedback");
        VBox.setMargin(inputArea, new Insets(10, 0, 5, 0));

        TextArea textArea = new TextArea(getTraceAsString(e));
        VBox.setMargin(textArea, new Insets(10, 0, 5, 0));
        textArea.setEditable(false);
        textArea.setWrapText(false);

        Button sendButton = new Button();
        sendButton.setText("send");
        sendButton.getStyleClass().add("send");

        sendButton.setOnAction(e1 -> {
            sendStackTrace(inputArea.getText(), textArea.getText());
            inputArea.setText("");
        });

        alert.getDialogPane().getChildren().add(sendButton);
        Button close = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        close.setText("close");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().add(sendButton);

        VBox.setVgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setVgrow(sendButton, Priority.ALWAYS);

        exceptionLabel = new Label(e.getMessage());
        VBox expansion = new VBox();

        expansion.getChildren().add(exceptionLabel);
        expansion.getChildren().add(feedbackLabel);
        expansion.getChildren().add(inputArea);
        expansion.getChildren().add(stackTraceLabel);
        expansion.getChildren().add(textArea);

        alert.getDialogPane().setContent(expansion);
        alert.setResizable(true);
    }

    public void showAndWait() {
        alert.showAndWait();
    }

    public void close() {
        alert.close();
    }

    private void sendStackTrace(String feedback, String stacktrace) {
        try {
            reportHandler.sendStacktrace(feedback, stacktrace);
        } catch (IOException e) {
            logger.error("failed to send crash report: " + e.getMessage(), e);
        } finally {
            alert.close();
        }
    }

    private String getTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public Alert getAlert() {
        return alert;
    }

    public Label getExceptionLabel() {
        return exceptionLabel;
    }

    public TextArea getInputArea() {
        return inputArea;
    }
}
