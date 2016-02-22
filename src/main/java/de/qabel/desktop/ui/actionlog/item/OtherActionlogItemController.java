package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import org.ocpsoft.prettytime.PrettyTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class OtherActionlogItemController extends AbstractController implements Initializable, ActionlogItem {
	ResourceBundle resourceBundle;

	@FXML
	Label dateLabel;
	@FXML
	Pane avatarContainer;
	@FXML
	Pane messageContainer;

	@Inject
	DropMessage dropMessage;
	@Inject
	Contact contact;
	@Inject
	MessageRendererFactory messageRendererFactory;

	PrettyTime p;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		MessageRenderer renderer = messageRendererFactory.getRenderer(dropMessage.getDropPayloadType());
		Node renderedMessage = renderer.render(dropMessage.getDropPayload());
		messageContainer.getChildren().addAll(renderedMessage);

		p = new PrettyTime(resources.getLocale());
		dateLabel.setText(p.format(dropMessage.getCreationDate()));

		dateLabel.setWrapText(true);

		dateLabel.setTextAlignment(TextAlignment.JUSTIFY);
		updateAvatar();
	}

	private void updateAvatar() {
		new AvatarView(e -> contact.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
	}


	@Override
	public void refreshDate() {
		Platform.runLater(()-> dateLabel.setText(p.format(dropMessage.getCreationDate())));
	}
}
