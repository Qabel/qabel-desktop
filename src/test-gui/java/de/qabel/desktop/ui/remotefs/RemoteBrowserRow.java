package de.qabel.desktop.ui.remotefs;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class RemoteBrowserRow extends AbstractPage {
    private RemoteFSController controller;
    private int rowIndex;

    public RemoteBrowserRow(FXRobot baseFXRobot, FxRobot robot, RemoteFSController controller, int rowIndex) {
        super(baseFXRobot, robot);
        this.controller = controller;
        this.rowIndex = rowIndex;
    }

    public RemoteFileDetailsPage share() {
        clickOn("#share_" + rowIndex);
        return waitForDetails();
    }

    public RemoteFileDetailsPage waitForDetails() {
        waitForNode(".detailsContainer");
        waitUntil(() -> getFirstNode(".detailsContainer").isVisible());
        waitUntil(() -> controller.fileDetails != null);
        return new RemoteFileDetailsPage(baseFXRobot, robot, controller.fileDetails);
    }

    public RowIcon shareIcon() {
        return getIcon("share");
    }

    private RowIcon getIcon(String prefix) {
        return new RowIcon(baseFXRobot, robot, controller, rowIndex, prefix);
    }

    public RowIcon downloadIcon() {
        return getIcon("download");
    }

    public RowIcon uploadFileIcon() {
        return getIcon("upload_file");
    }

    public RowIcon uploadFolderIcon() {
        return getIcon("upload_folder");
    }

    public RowIcon createFolderIcon() {
        return getIcon("create_folder");
    }

    public RowIcon deleteIcon() {
        return getIcon("delete");
    }
}
