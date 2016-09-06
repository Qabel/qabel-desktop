package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.accounting.identity.IdentityEditViewPage;
import org.testfx.api.FxRobot;

class IdentityContextMenuPage extends AbstractPage {
    private static final String CLOSE_MENU = "";
    private IdentityContextMenuController controller;

    private static String OPEN_ID_EDIT = "#editButton";
    private static String OPEN_QR = "#openQRCode";

    IdentityContextMenuPage(FXRobot baseFXRobot, FxRobot robot, IdentityContextMenuController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    void openIdentityEdit() {
        clickOn(OPEN_ID_EDIT);
    }

    public void openQrCode() {
        clickOn(OPEN_QR);
    }

    void changeIdentity(String alias, String email, String phone) {
        waitUntil(() -> controller.identityEditController != null);
        IdentityEditViewPage identityEditPage = new IdentityEditViewPage(baseFXRobot, robot, controller.identityEditController);

        identityEditPage.clearFields();
        identityEditPage.enterAlias(alias);
        identityEditPage.enterEmail(email);
        identityEditPage.enterPhone(phone);
        identityEditPage.presSave();
    }

    public void closeMenu() {
        clickOn(CLOSE_MENU);
    }
}
