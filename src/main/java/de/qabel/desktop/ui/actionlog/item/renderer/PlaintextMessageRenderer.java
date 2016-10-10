package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.util.ResourceBundle;

public class PlaintextMessageRenderer implements FXMessageRenderer {

    @Override
    public Node render(String dropPayload, ResourceBundle resourceBundle) {
        Label label = new Label(renderString(dropPayload, resourceBundle));
        label.getStyleClass().add("message-text");
        return makeSelectable(label);
    }

    private Label makeSelectable(Label label) {
        StackPane textStack = new StackPane();
        textStack.getStyleClass().add("selectable-text");

        TextArea textField = new TextArea(label.getText());
        textField.setEditable(false);
        textField.setPrefRowCount(1);
        // the invisible label is a hack to get the textField to size like a label.
        Label invisibleLabel = new Label();
        invisibleLabel.textProperty().bind(label.textProperty());
        invisibleLabel.setVisible(false);
        textStack.getChildren().addAll(invisibleLabel, textField);

        label.textProperty().bindBidirectional(textField.textProperty());
        label.setGraphic(textStack);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        return label;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }
}
