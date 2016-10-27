package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.partial.HyperlinkRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.partial.PartialFXMessageRenderer;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class PlaintextMessageRenderer implements FXMessageRenderer {
    private final HyperlinkRenderer hyperlinkRenderer = new HyperlinkRenderer();

    @Override
    public TextFlow render(String prefixAlias, String dropPayload, ResourceBundle resourceBundle) {
        TextFlow flow = new TextFlow();
        flow.getChildren().setAll(renderTextFlowElements(prefixAlias, renderString(dropPayload, resourceBundle)));
        return flow;
    }

    @NotNull
    List<Node> renderTextFlowElements(String prefixAlias, String message) {
        List<Node> children = new LinkedList<>();
        children.add(new Text(message));

        replaceRenderableChildren(children);

        children.add(0, renderAlias(prefixAlias));
        return children;
    }

    private void replaceRenderableChildren(List<Node> children) {
        List<PartialFXMessageRenderer> partialRenderers = new LinkedList<>();
        partialRenderers.add(hyperlinkRenderer);

        int size;
        do {
            size = children.size();
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                if (!(child instanceof Text))
                    continue;

                for (PartialFXMessageRenderer renderer : partialRenderers) {
                    String unrenderedText = ((Text) child).getText();
                    if (renderer.needsFormatting(unrenderedText)) {
                        List<Node> replacement = renderer.render(unrenderedText);
                        children.remove(i);
                        children.addAll(i, replacement);
                        i = i + replacement.size() - 1;
                        break;
                    }
                }
            }
        } while (size != children.size());
    }

    private Node renderAlias(String alias) {
        Text node = new Text(alias + "   ");
        node.getStyleClass().add("alias");
        node.managedProperty().bind(node.visibleProperty());
        return node;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }

    public HyperlinkRenderer getHyperlinkRenderer() {
        return hyperlinkRenderer;
    }
}
