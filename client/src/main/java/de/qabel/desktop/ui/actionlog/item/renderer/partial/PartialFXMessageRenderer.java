package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import javafx.scene.Node;

import java.util.List;

public interface PartialFXMessageRenderer {
    boolean needsFormatting(String text);
    List<Node> render(String text);
}
