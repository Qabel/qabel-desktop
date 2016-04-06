package de.qabel.desktop.ui.actionlog.item.renderer;

import javafx.scene.Node;

import java.util.ResourceBundle;

public interface MessageRenderer {
    Node render(String dropPayload, ResourceBundle resourceBundle);
}
