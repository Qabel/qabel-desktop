package de.qabel.desktop.ui.actionlog.item.renderer;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

public class PlaintextMessageRenderer implements MessageRenderer {
	@Override
	public Node render(String dropPayload, ResourceBundle resourceBundle) {
		Label label = new Label(dropPayload);
		label.getStyleClass().add("message-text");
		return label;
	}
}
