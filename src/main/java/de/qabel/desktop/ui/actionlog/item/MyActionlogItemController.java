package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.ocpsoft.prettytime.PrettyTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class MyActionlogItemController extends AbstractController implements Initializable, ActionlogItem {
    ResourceBundle resourceBundle;

    @FXML
    Pane messageContainer;
    @FXML
    Label dateLabel;

    @Inject
    private DropMessage dropMessage;
    @Inject
    MessageRendererFactory messageRendererFactory;

    PrettyTime p;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MessageRenderer renderer = messageRendererFactory.getRenderer(dropMessage.getDropPayloadType());
        Node renderedMessage = renderer.render(dropMessage.getDropPayload(), resources);
        renderedMessage.getStyleClass().add("sent");
        messageContainer.getChildren().addAll(renderedMessage);

        p = new PrettyTime(resources.getLocale());
        dateLabel.setText(p.format(dropMessage.getCreationDate()));
    }


    @Override
    public void refreshDate() {
        Platform.runLater(()-> dateLabel.setText(p.format(dropMessage.getCreationDate())));
    }


    public Label getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(Label dateLabel) {
        this.dateLabel = dateLabel;
    }

    public DropMessage getDropMessage() {
        return dropMessage;
    }

    public void setDropMessage(DropMessage dropMessage) {
        this.dropMessage = dropMessage;
    }
}
