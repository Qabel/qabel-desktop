package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import de.qabel.desktop.ui.actionlog.Hyperlink;
import javafx.event.Event;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyperlinkRenderer extends AbstractPatternExtractionRenderer {
    Consumer<String> browserOpener = (uri) -> new Thread(() -> {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException | URISyntaxException ignored) {}
    }).start();
    private static final Pattern LINK_PATTERN = Pattern.compile("(\\w+:\\/\\/(?:www\\.|(?!www))[^\\s\\.]+\\.[^\\s]{2,}|www\\.[^\\s]+\\.[^\\s]{2,})");

    @Override
    protected Pattern getPattern() {
        return LINK_PATTERN;
    }

    @Override
    protected Text replace(String match, Matcher matcher) {
        Hyperlink hyperlink = new Hyperlink(match);
        hyperlink.setOnAction(this::openUriInBrowser);
        return hyperlink;
    }

    private void openUriInBrowser(Event event) {
        Text link = (Text) event.getTarget();
        final String uri = link == null ? "" : link.getText();
        browserOpener.accept(uri);
        event.consume();
    }

    public void setBrowserOpener(Consumer<String> browserOpener) {
        this.browserOpener = browserOpener;
    }
}
