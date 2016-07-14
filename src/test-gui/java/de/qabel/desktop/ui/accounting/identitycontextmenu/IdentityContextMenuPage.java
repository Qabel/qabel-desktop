package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class IdentityContextMenuPage extends AbstractPage {
    private IdentityContextMenuController controller;

    public IdentityContextMenuPage(FXRobot baseFXRobot, FxRobot robot, IdentityContextMenuController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void edit() {
        controller.setAlias("new alias");
    }
}
