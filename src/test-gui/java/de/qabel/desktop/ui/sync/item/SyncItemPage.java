package de.qabel.desktop.ui.sync.item;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.ConfirmationPage;
import javafx.scene.control.Labeled;
import org.testfx.api.FxRobot;

public class SyncItemPage extends AbstractPage {
	private final SyncItemController controller;

	public SyncItemPage(FXRobot baseFXRobot, FxRobot robot, SyncItemController controller) {
		super(baseFXRobot, robot);
		this.controller = controller;
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
}
