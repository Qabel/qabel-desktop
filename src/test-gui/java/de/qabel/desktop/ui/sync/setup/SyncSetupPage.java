package de.qabel.desktop.ui.sync.setup;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class SyncSetupPage extends AbstractPage {
    private SyncSetupController controller;

    public SyncSetupPage(FXRobot baseFXRobot, FxRobot robot, SyncSetupController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void enterName(String name) {
        clickOn("#name").write(name);
    }

    public void enterLocalPath(String localPath) {
        clickOn("#localPath").write(localPath);
    }

    public void enterRemotePath(String remotePath) {
        clickOn("#remotePath").write(remotePath);
    }

    public void startSync() {
        clickOn("#start");
    }
}
