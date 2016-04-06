package de.qabel.desktop.ui.remotefs;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

import java.util.List;

public class RemoteFileDetailsPage extends AbstractPage {
    private RemoteFileDetailsController controller;

    public RemoteFileDetailsPage(FXRobot baseFXRobot, FxRobot robot, RemoteFileDetailsController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public RemoteFileDetailsPage shareBySearch(String contactSearchTerm, String shareMessage) {
        clickOn(getFirstNode("#shareReceiver")).write(contactSearchTerm).push(KeyCode.ENTER);
        waitUntil(() -> controller.dialog != null);
        runLaterAndWait(() -> controller.dialog.getEditor().setText(shareMessage));
        clickOn(controller.dialog.getDialogPane().lookupButton(ButtonType.OK));
        return this;
    }

    public void close() {
        clickOn(".details .close");
    }

    public RemoteFileDetailsPage shareFirst(String shareMessage) {
        FxRobot fxRobot = clickOn("#shareReceiver .arrow");
        while (true) {
            try {
                getFirstNode("#detailsContainer .list-cell");
                break;
            } catch (Exception ignored) {
                fxRobot = clickOn("#shareReceiver .arrow");
            }
        }
        fxRobot.push(KeyCode.DOWN);
        waitUntil(() -> controller.dialog != null);
        runLaterAndWait(() -> controller.dialog.getEditor().setText(shareMessage));
        baseFXRobot.waitForIdle();
        clickOn(controller.dialog.getDialogPane().lookupButton(ButtonType.OK));
        waitUntil(() -> controller.dialog == null);
        return this;
    }

    public List getCurrentReceivers() {
        return controller.currentShares.getChildren();
    }

    public void assertReceivers(int receiverCount) {
        waitUntil(() -> getCurrentReceivers().size() == receiverCount);
    }

    public RemoteFileDetailsPage unshare() {
        clickOn("#unshare");

        waitUntil(() -> controller.confirmationDialog != null);
        clickOn(controller.confirmationDialog.getDialogPane().lookupButton(ButtonType.YES));
        return this;
    }
}
