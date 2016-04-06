package de.qabel.desktop.ui.sync;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class SyncPage extends AbstractPage {
    private SyncController controller;

    public SyncPage(FXRobot baseFXRobot, FxRobot robot, SyncController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void add() {
        controller.addStage = null;
        clickOn("#addSync");
        waitUntil(() -> controller.addStage != null);
        waitUntil(() -> controller.addStage.isShowing());
    }
}
