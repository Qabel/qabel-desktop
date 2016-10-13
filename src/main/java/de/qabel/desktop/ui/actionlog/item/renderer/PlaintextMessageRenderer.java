package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.HyperlinkLabel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaintextMessageRenderer implements FXMessageRenderer {
    private static final String STYLE_CLASS = "message-text";
    public Consumer<String> browserOpener;

    public PlaintextMessageRenderer() {
        browserOpener = (uri) -> {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (IOException | URISyntaxException ignored) {
            }
        };

    }

    @Override
    public Node render(String dropPayload, ResourceBundle resourceBundle) {
        String text = renderString(dropPayload, resourceBundle);
        return renderHyperlinks(text);
    }

    @NotNull
    TextFlow renderHyperlinks(String message) {
        String regex = "(https?:\\/\\/(?:www\\.|(?!www))[^\\s\\.]+\\.[^\\s]{2,}|www\\.[^\\s]+\\.[^\\s]{2,})";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(message);
        String result = matcher.replaceAll("[$1]");

        HyperlinkLabel hyperLinkLabel = new HyperlinkLabel(result);
        hyperLinkLabel.getStyleClass().add("text");

        hyperLinkLabel.setOnAction(event -> {
            Hyperlink link = (Hyperlink) event.getSource();
            final String uri = link == null ? "" : link.getText();
            new Thread(() -> browserOpener.accept(uri)).start();
        });

        TextFlow node = new TextFlow(hyperLinkLabel);
        node.getStyleClass().add(STYLE_CLASS);
        return node;
    }

    @NotNull
    private Label renderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(STYLE_CLASS);
        return label;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }


}
