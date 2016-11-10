package de.qabel.desktop.ui;

import com.sun.javafx.robot.FXRobot;
import javafx.scene.control.Alert;
import org.testfx.api.FxRobot;

public class CrashReportAlertPage extends AbstractPage {
    private CrashReportAlert alert;

    public CrashReportAlertPage(FXRobot baseFXRobot, FxRobot robot, CrashReportAlert alert) {
        super(baseFXRobot, robot);
        this.alert = alert;
    }

    public String getTitle() {
        return alert.getAlert().getTitle();
    }

    public String getHeaderText() {
        return alert.getAlert().getHeaderText();
    }

    public Alert.AlertType getType() {
        return alert.getAlert().getAlertType();
    }

    public String getExceptionLabel() {
        return alert.getExceptionLabel().getText();
    }

    public CrashReportAlertPage setFeedback(String message) {
        clickOn(".feedback").write(message);
        return this;
    }

    public CrashReportAlertPage send() {
        clickOn(".sendEvent");
        waitUntil(() -> alert.getInputArea().getText().isEmpty());
        return this;
    }
}
