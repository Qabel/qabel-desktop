package de.qabel.desktop.ui.actionlog;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class ActionLogPage extends AbstractPage {
    private final ActionlogController controller;

    public ActionLogPage(FXRobot baseFXRobot, FxRobot robot, ActionlogController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public boolean isNotificationShown() {
        return controller.notification.isVisible();
    }

}
