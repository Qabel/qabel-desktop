package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.fxmisc.richtext.InlineCssTextArea;

import java.util.ResourceBundle;

public class PlaintextMessageRenderer implements FXMessageRenderer {

    @Override
    public Node render(String dropPayload, ResourceBundle resourceBundle) {
        Label label = new Label(renderString(dropPayload, resourceBundle));
        label.getStyleClass().add("message-text");

        return createTransparentBackgroundTextArea(label);
    }

    private InlineCssTextArea createTransparentBackgroundTextArea(Label label) {
        InlineCssTextArea textArea = new InlineCssTextArea(label.getText());
        textArea.setWrapText(true);
        textArea.getStyleClass().add("message-text");
        textArea.setEditable(false);
        return textArea;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }
}
