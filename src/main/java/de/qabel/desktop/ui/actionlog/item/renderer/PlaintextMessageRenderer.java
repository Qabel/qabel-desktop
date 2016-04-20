package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

public class PlaintextMessageRenderer implements FXMessageRenderer {
    @Override
    public Node render(String dropPayload, ResourceBundle resourceBundle) {
        Label label = new Label(renderString(dropPayload, resourceBundle));
        label.getStyleClass().add("message-text");
        return label;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }
}
