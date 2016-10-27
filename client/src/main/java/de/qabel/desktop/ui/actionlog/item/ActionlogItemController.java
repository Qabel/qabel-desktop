package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class ActionlogItemController extends AbstractController implements Initializable, ActionlogItem {
    @FXML
    Node messageItem;

    @FXML
    HBox messageContainer;

    @FXML
    Label dateLabel;

    @Inject
    String sender;
    @Inject
    private DropMessage dropMessage;
    @Inject
    FXMessageRendererFactory messageRendererFactory;

    private SimpleDateFormat p = new SimpleDateFormat("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateLabel.setText(p.format(dropMessage.getCreationDate()));
        messageContainer.getChildren().setAll(getRenderedMessage(sender, resources));
    }

    @NotNull
    private Node getRenderedMessage(String prefixAlias, ResourceBundle resources) {
        FXMessageRenderer renderer = messageRendererFactory.getRenderer(dropMessage.getDropPayloadType());
        return renderer.render(prefixAlias, dropMessage.getDropPayload(), resources);
    }

    @Override
    public void refreshDate() {
        Platform.runLater(() -> dateLabel.setText(p.format(dropMessage.getCreationDate())));
    }

    public Label getDateLabel() {
        return dateLabel;
    }

    public DropMessage getDropMessage() {
        return dropMessage;
    }

    public void setDropMessage(DropMessage dropMessage) {
        this.dropMessage = dropMessage;
    }
}
