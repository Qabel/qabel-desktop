package de.qabel.desktop.ui.remotefs;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class RowIcon extends AbstractPage {
	private String prefix;
	private RemoteFSController controller;
	private int rowIndex;

	public RowIcon(FXRobot baseFXRobot, FxRobot robot, RemoteFSController controller, int rowIndex, String prefix) {
		super(baseFXRobot, robot);
		this.controller = controller;
		this.rowIndex = rowIndex;
		this.prefix = prefix;
	}

	public void assertHidden() {
		waitUntil(() -> !waitForNode(selector()).isVisible());
	}

	public void assertVisible() {
		waitUntil(() -> waitForNode(selector()).isVisible());
	}

	private String selector() {
		return "#" + prefix + "_" + rowIndex;
	}

	public RowIcon hover() {
		moveTo(selector());
		return this;
	}
}
