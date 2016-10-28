package de.qabel.desktop.ui.actionlog.item.renderer;

import javafx.scene.Node;

import java.util.ResourceBundle;

public interface FXMessageRenderer extends MessageRenderer {
    Node render(String prefixAlias, String dropPayload, ResourceBundle resourceBundle);
}
