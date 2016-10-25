package de.qabel.desktop.ui.actionlog.util;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class QabelChatSkin extends BehaviorSkinBase<QabelChatLabel, BehaviorBase<QabelChatLabel>> {

    // The strings used to delimit the hyperlinks
    private static final String HYPERLINK_START = "["; //$NON-NLS-1$
    private static final String HYPERLINK_END = "]"; //$NON-NLS-1$
    private TextFlow textFlow;
    private final Text senderAlias;

    QabelChatSkin(String senderAlias, QabelChatLabel control) {
        super(control, new BehaviorBase<>(control, Collections.emptyList()));

        textFlow = new TextFlow();

        this.senderAlias = new Text(senderAlias + " ");
        this.senderAlias.getStyleClass().add("message-sendername");

        getChildren().addAll(textFlow);
        updateText();

        registerChangeListener(control.textProperty(), "TEXT"); //$NON-NLS-1$
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);

        if (p.equals("TEXT")) { //$NON-NLS-1$
            updateText();
        }
    }

    private void updateText() {
        final String text = getSkinnable().getText();
        if (text == null || text.isEmpty()) {
            textFlow.getChildren().clear();
            return;
        }
        // parse the text and put it into an array list
        final List<Node> nodes = new ArrayList<>();

        nodes.add(senderAlias);

        int start = 0;
        final int textLength = text.length();
        while (start != -1 && start < textLength) {
            int startPos = text.indexOf(HYPERLINK_START, start);
            int endPos = text.indexOf(HYPERLINK_END, startPos);

            if (isNotHyperlink(startPos, endPos)) {
                if (textLength > start) {
                    // ...but there is still text to turn into one last label
                    appendTextNode(nodes, text.substring(start));
                    break;
                }
            }
            appendTextNode(nodes, text.substring(start, startPos));
            appendHyperlink(nodes, text.substring(startPos + 1, endPos));
            start = endPos + 1;
        }
        textFlow.setMaxWidth(Control.USE_PREF_SIZE);
        textFlow.getChildren().setAll(nodes);
        textFlow.requestLayout();
    }

    private boolean isNotHyperlink(int startPos, int endPos) {
        return startPos == -1 || endPos == -1;
    }

    private void appendTextNode(List<Node> nodes, String content) {
        Text textnode = new Text(content);
        nodes.add(textnode);
    }

    private void appendHyperlink(List<Node> nodes, String content) {
        Text hyperlink = new Text(content);
        hyperlink.getStyleClass().add("hyperlink");
        hyperlink.onMouseClickedProperty().bind(getSkinnable().onMouseClickedProperty());
        nodes.add(hyperlink);
    }
}
