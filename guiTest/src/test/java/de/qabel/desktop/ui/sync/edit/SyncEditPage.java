package de.qabel.desktop.ui.sync.edit;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class SyncEditPage extends AbstractPage {
    private SyncEditController controller;

    public SyncEditPage(FXRobot baseFXRobot, FxRobot robot, SyncEditController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
        if (controller.getStage() != null) {
            robot.targetWindow(controller.getStage());
        }
    }

    public SyncEditPage enterName(String name) {
        controller.setName("");
        controller.setName(name);
        return this;
    }

    public SyncEditPage enterLocalPath(String localPath) {
        controller.setLocalPath("");
        controller.setLocalPath(localPath);
        return this;
    }

    public SyncEditPage enterRemotePath(String remotePath) {
        controller.setRemotePath("");
        controller.setRemotePath(remotePath);
        return this;
    }

    public void save() {
        robot.interact(controller::save);
        waitUntil(() -> !controller.getStage().isShowing());
    }
}
