package de.qabel.desktop.ui.actionlog.item.renderer;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class PlaintextMessageRenderer implements MessageRenderer {
	@Override
	public Node render(String dropPayload) {
		Label label = new Label(dropPayload);
		label.getStyleClass().add("message-text");
		return label;
	}
}
