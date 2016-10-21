package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.HyperlinkLabel;
import org.ocpsoft.prettytime.PrettyTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class NewActionlogItemController extends AbstractController implements Initializable, ActionlogItem {

    @FXML
    Label senderName;

    @FXML
    TextFlow messageContainer;

    @FXML
    Label dateLabel;

    @Inject
    String sender;
    @Inject
    private DropMessage dropMessage;
    @Inject
    FXMessageRendererFactory messageRendererFactory;

    private PrettyTime p;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageContainer.getChildren().clear();

        p = new PrettyTime(resources.getLocale());
        dateLabel.setText(p.format(dropMessage.getCreationDate()));

        senderName.setText(sender);

        FXMessageRenderer renderer = messageRendererFactory.getRenderer(dropMessage.getDropPayloadType());
        Node renderedMessage = renderer.render(dropMessage.getDropPayload(), resources);
        renderedMessage.getStyleClass().add("sent");
        messageContainer.getChildren().addAll(renderedMessage);


    }

    @Override
    public void refreshDate() {
        Platform.runLater(() -> dateLabel.setText(p.format(dropMessage.getCreationDate())));
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
