package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.InputDialogPage;
import de.qabel.desktop.ui.accounting.identityContextMenu.IdentityContextMenuController;
import org.testfx.api.FxRobot;

public class IdentityContextMenuPage extends AbstractPage {
    private IdentityContextMenuController controller;

    public IdentityContextMenuPage(FXRobot baseFXRobot, FxRobot robot, IdentityContextMenuController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public InputDialogPage edit() {
        controller.dialog = null;
        clickOn("#editButton");
        //waitUntil(() -> controller.dialog != null);
        return new InputDialogPage(baseFXRobot, robot, controller.dialog);
    }
}
