package de.qabel.desktop.ui.actionlog.util;

import javafx.scene.control.Skin;
import org.controlsfx.control.HyperlinkLabel;

public class QabelChatLabel extends HyperlinkLabel {

    public QabelChatLabel() {
        this(null);
    }

    public QabelChatLabel(String text) {
        super(text);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new QabelChatSkin(this);
    }
}
