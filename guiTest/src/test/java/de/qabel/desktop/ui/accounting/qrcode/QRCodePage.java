package de.qabel.desktop.ui.accounting.qrcode;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class QRCodePage extends AbstractPage {
    private QRCodeController controller;

    public QRCodePage(FXRobot baseFXRobot, FxRobot robot, QRCodeController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void showPopup() {
        runLaterAndWait(controller::show);
        waitUntil(controller::isVisible);
    }
}
