package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import de.qabel.desktop.ui.util.Icons;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiRenderer extends AbstractPatternExtractionRenderer {
    private static final Pattern ALIAS_PATTERN = Pattern.compile("(:(\\w|\\||\\-)+:)");
    private static final Logger logger = LoggerFactory.getLogger(EmojiRenderer.class);

    @Override
    public boolean needsFormatting(String text) {
        return super.needsFormatting(EmojiParser.parseToAliases(text));
    }

    @Override
    protected Pattern getPattern() {
        return ALIAS_PATTERN;
    }

    @Override
    public List<Node> render(String text) {
        return super.render(EmojiParser.parseToAliases(text));
    }

    @Override
    protected Node replace(String match, Matcher matcher) {
        if (!match.startsWith(":") || !match.endsWith(":")) {
            return new Text(match);
        }
        try {
            ImageView icon = Icons.getIcon(EmojiManager.getForAlias(match), 24);
            icon.getStyleClass().add("emoji");
            Label emojiLabel = new Label("", icon);
            emojiLabel.getStyleClass().add("emoji-container");
            emojiLabel.setPadding(new Insets(0, 1, 0, 1));
            return emojiLabel;
        } catch (Exception e) {
            logger.error("failed to render emoji " + match, e);
            return renderFallback(match);
        }
    }

    @NotNull
    private Text renderFallback(String match) {
        return new Text(EmojiParser.parseToUnicode(match));
    }
}
