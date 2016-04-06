package de.qabel.desktop.ui;

import com.sun.javafx.robot.FXRobot;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.testfx.api.FxRobot;

public class ConfirmationPage extends AbstractPage {
    private Alert dialog;

    public ConfirmationPage(FXRobot baseFXRobot, FxRobot robot, Alert dialog) {
        super(baseFXRobot, robot);
        this.dialog = dialog;
    }

    public void yes() {
        clickOn(dialog.getDialogPane().lookupButton(ButtonType.YES));
    }

    public void cancel() {
        clickOn(dialog.getDialogPane().lookupButton(ButtonType.CANCEL));
    }

    public ConfirmationPage waitFor() {
        waitUntil(dialog.getDialogPane()::isVisible);
        return this;
    }
}
