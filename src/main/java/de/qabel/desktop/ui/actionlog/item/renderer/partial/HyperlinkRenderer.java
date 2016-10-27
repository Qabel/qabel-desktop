package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import de.qabel.desktop.ui.actionlog.Hyperlink;
import javafx.event.Event;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyperlinkRenderer extends AbstractPatternExtractionRenderer {
    private static final Logger logger = LoggerFactory.getLogger(HyperlinkRenderer.class);
    /**
     * scheme + something | scheme + something with a dot
     */
    private static final Pattern GENERIC_URL_PATTERN = Pattern.compile("(((\\w+:\\/\\/)|((\\w+:\\/\\/)?[^\\/?#\\s]+\\.))[^?#\\s]+)");
    private static final Pattern IP_PATTERN = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    Consumer<String> browserOpener = (uri) -> new Thread(() -> {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException | URISyntaxException ignored) {}
    }).start();

    @Override
    protected Pattern getPattern() {
        return GENERIC_URL_PATTERN;
    }

    @Override
    protected Text replace(String match, Matcher matcher) {
        String plaintext = match;
        try {
            match = checkUri(match);

            Hyperlink hyperlink = new Hyperlink(match);
            hyperlink.setOnAction(this::openUriInBrowser);
            return hyperlink;
        } catch (URISyntaxException ignored) {}
        return new Text(plaintext);
    }

    @NotNull
    private String checkUri(String match) throws URISyntaxException {
        URI uri;
        try {
            uri = new URI(match);
            checkScheme(match, uri);
            return match;
        } catch (URISyntaxException e) {
            match = "http://" + match;
            uri = new URI(match);
            if (hasValidTld(uri) || isIp(uri)) {
                return match;
            }
        }
        logger.debug("rejecting " + match + " with host " + uri.getHost());
        throw new URISyntaxException(match, "url is no ip and has no valid tld");
    }

    private boolean isIp(URI uri) {
        return IP_PATTERN.matcher(uri.getHost()).matches();
    }

    private void checkScheme(String match, URI uri) throws URISyntaxException {
        if (uri.getScheme() == null) {
            throw new URISyntaxException(match, "missing scheme");
        }
    }

    private boolean hasValidTld(URI uri) throws URISyntaxException {
        String host = uri.getHost();
        String tld = host.substring(host.lastIndexOf(".") + 1);
        return TLD.isValid(tld);
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
