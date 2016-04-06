package de.qabel.desktop.ui;

import com.sun.javafx.robot.FXRobot;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.testfx.api.FxRobot;

public class InputDialogPage extends AbstractPage {
    private final TextInputDialog dialog;

    public InputDialogPage(FXRobot baseFXRobot, FxRobot robot, TextInputDialog dialog) {
        super(baseFXRobot, robot);
        this.dialog = dialog;
    }

    public void inputAndConfirm(String text) {
        setText(text).confirm();
    }

    public InputDialogPage setText(String text) {
        runLaterAndWait(() -> dialog.getEditor().setText(text));
        return this;
    }

    public InputDialogPage confirm() {
        clickOn(dialog.getDialogPane().lookupButton(ButtonType.OK));
        return this;
    }
}
