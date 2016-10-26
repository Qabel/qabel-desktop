package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import javafx.scene.Node;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPatternExtractionRenderer implements PartialFXMessageRenderer {
    @Override
    public boolean needsFormatting(String text) {
        return getPattern().matcher(text).find();
    }

    protected abstract Pattern getPattern();

    @Override
    public List<Node> render(String text) {
        List<Node> result = new LinkedList<>();
        Matcher matcher = getPattern().matcher(text);

        int position = 0;
        while (matcher.find()) {
            if (matcher.start() > position) {
                result.add(new Text(text.substring(position, matcher.start())));
            }
            result.add(replace(matcher.group(), matcher));
            position = matcher.end();
        }
        if (position < text.length() - 1) {
            result.add(new Text(text.substring(position)));
        }
        return result;
    }

    protected abstract Node replace(String match, Matcher matcher);
}
