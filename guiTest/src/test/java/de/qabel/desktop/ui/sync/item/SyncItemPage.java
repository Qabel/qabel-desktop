package de.qabel.desktop.ui.sync.item;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.ConfirmationPage;
import de.qabel.desktop.ui.sync.edit.SyncEditPage;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import org.testfx.api.FxRobot;

public class SyncItemPage extends AbstractPage {
    private final SyncItemController controller;
    private String rootClass;

    public SyncItemPage(FXRobot baseFXRobot, FxRobot robot, SyncItemController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
        rootClass = "." + controller.getSyncConfig().getName();
    }

    public ConfirmationPage delete() {
        controller.confirmationDialog = null;
        clickOn("#deleteSync");
        waitUntil(() -> controller.confirmationDialog != null);
        return new ConfirmationPage(baseFXRobot, robot, controller.confirmationDialog).waitFor();
    }

    public String remotePath() {
        return ((Labeled)getFirstNode("#remotePath")).getText();
    }

    public String localPath() {
        return ((Labeled)getFirstNode("#localPath")).getText();
    }

    public String name() {
        return ((Labeled) getFirstNode("#name")).getText();
    }

    public String avatar() {
        return ((Labeled) getFirstNode("#avatar")).getText();
    }

    public SyncEditPage edit() {
        controller.syncEditController = null;
        fakeClick("#editSync");
        waitUntil(() -> controller.syncEditController != null
            && controller.syncEditController.getStage() != null
            && controller.syncEditController.getStage().isShowing());
        FxRobot robot = new FxRobot();
        robot.targetWindow(controller.editStage);
        return new SyncEditPage(baseFXRobot, robot, controller.syncEditController);
    }

    @Override
    protected Node getFirstNode(String query) {
        return super.getFirstNode(rootClass + ".syncItem " + query);
    }
}
