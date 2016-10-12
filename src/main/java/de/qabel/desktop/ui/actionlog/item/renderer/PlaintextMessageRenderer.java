package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.daemon.drop.TextMessage;
import javafx.application.Platform;
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
        label.setWrapText(true);
        label.getStyleClass().add("message-text");

        return makeSelectable(label);
    }

    private Label makeSelectable(final Label label) {
        StackPane textStack = new StackPane();
        final TextArea textArea = new TextArea();
        textArea.getStyleClass().addAll(label.getStyleClass());
        textArea.textProperty().bind(label.textProperty());
        textArea.setEditable(false);
        textArea.setPrefRowCount(1);

        Platform.runLater(() -> {
            textArea.setPrefSize(label.getWidth(), label.getHeight());
        });

        // the invisible label is a hack to get the textField to size like a label (maybe it is not really necessary...)
        Label invisibleLabel = new Label();
        invisibleLabel.setWrapText(false);
        invisibleLabel.textProperty().bind(label.textProperty());
        invisibleLabel.setVisible(false);
        textStack.getChildren().addAll(invisibleLabel, textArea);
        textArea.textProperty().bind(label.textProperty());
        label.setGraphic(textStack);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        return label;
    }

    @Override
    public String renderString(String dropPayload, ResourceBundle resourceBundle) {
        return TextMessage.fromJson(dropPayload).getText();
    }
}
