package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.accounting.identity.IdentityEditViewPage;
import org.testfx.api.FxRobot;

public class IdentityContextMenuPage extends AbstractPage {
    private IdentityContextMenuController controller;

    private static String OPEN_ID_EDIT = "#editButton";
    private static String OPEN_QR = "#publicKeyQRButton";

    public IdentityContextMenuPage(FXRobot baseFXRobot, FxRobot robot, IdentityContextMenuController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    IdentityEditViewPage openIdentityEdit() {
        clickOn(OPEN_ID_EDIT);
        waitUntil(() -> controller.identityEditController != null);
        return new IdentityEditViewPage(baseFXRobot, robot, controller.identityEditController);
    }

    public void openQrCode() {
        clickOn(OPEN_QR);
        waitUntil(() -> controller.qrcodeController != null);
        waitUntil(this::qrcodeIsVisible);
    }

    boolean qrcodeIsVisible() {
        return controller.qrcodeController.isVisible();
    }
}
