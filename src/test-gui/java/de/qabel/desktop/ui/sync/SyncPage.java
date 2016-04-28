package de.qabel.desktop.ui.sync;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.sync.item.SyncItemController;
import de.qabel.desktop.ui.sync.item.SyncItemPage;
import org.testfx.api.FxRobot;

public class SyncPage extends AbstractPage {
    private SyncController controller;

    public SyncPage(FXRobot baseFXRobot, FxRobot robot, SyncController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void add() {
        controller.addStage = null;
        clickOn(".sync #addSync");
        waitUntil(() -> controller.addStage != null);
        waitUntil(() -> controller.addStage.isShowing());
    }

    public SyncItemPage getSync(String syncName) {
        for (SyncItemController itemController : controller.syncItemControllers) {
            if (itemController.getSyncConfig().getName().equals(syncName)) {
                return new SyncItemPage(baseFXRobot, robot, itemController);
            }
        }
        throw new IllegalStateException("no sync found for name " + syncName);
    }
}
