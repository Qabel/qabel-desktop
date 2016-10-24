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

public class QabelChatSkin extends BehaviorSkinBase<QabelChatLabel, BehaviorBase<QabelChatLabel>> {

    // The strings used to delimit the hyperlinks
    private static final String HYPERLINK_START = "["; //$NON-NLS-1$
    private static final String HYPERLINK_END = "]"; //$NON-NLS-1$
    private TextFlow textFlow;

    protected QabelChatSkin(QabelChatLabel control) {
        super(control, new BehaviorBase<>(control, Collections.emptyList()));

        textFlow = new TextFlow();
        textFlow.setMaxWidth(Control.USE_PREF_SIZE);
        textFlow.setMaxHeight(Control.USE_PREF_SIZE);

        getChildren().add(textFlow);
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

        int start = 0;
        final int textLength = text.length();
        while (start != -1 && start < textLength) {
            int startPos = text.indexOf(HYPERLINK_START, start);
            int endPos = text.indexOf(HYPERLINK_END, startPos);

            // if the startPos is -1, there are no more hyperlinks...
            if (startPos == -1 || endPos == -1) {
                if (textLength > start) {
                    // ...but there is still text to turn into one last label
                    Text label = new Text(text.substring(start));
                    nodes.add(label);
                    break;
                }
            }

            // firstly, create a label from start to startPos
            Text label = new Text(text.substring(start, startPos));
            nodes.add(label);

            // if endPos is greater than startPos, create a hyperlink
            Text hyperlink = new Text(text.substring(startPos + 1, endPos));
            hyperlink.getStyleClass().add("hyperlink");
            hyperlink.onMouseClickedProperty().bind(getSkinnable().onMouseClickedProperty());
            nodes.add(hyperlink);

            start = endPos + 1;
        }

        textFlow.getChildren().setAll(nodes);

    }
}
