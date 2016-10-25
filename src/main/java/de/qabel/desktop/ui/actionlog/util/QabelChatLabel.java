package de.qabel.desktop.ui.actionlog.util;

import javafx.scene.control.Skin;
import org.controlsfx.control.HyperlinkLabel;

public class QabelChatLabel extends HyperlinkLabel {

    private String prefixAlias;

    public QabelChatLabel(String prefixAlias, String text) {
        super(text);
        this.prefixAlias = prefixAlias;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new QabelChatSkin(prefixAlias, this);
    }
}
