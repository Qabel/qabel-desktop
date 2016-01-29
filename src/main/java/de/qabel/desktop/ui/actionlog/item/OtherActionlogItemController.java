package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.ocpsoft.prettytime.PrettyTime;

import javax.inject.Inject;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;

public class OtherActionlogItemController extends AbstractController implements Initializable, ActionlogItem {

	ResourceBundle resourceBundle;

	@FXML
	Label textlabel;
	@FXML
	Label dateLabel;
	@FXML
	Pane avatarContainer;

	@Inject
	DropMessage dropMessage;
	@Inject
	Contact contact;


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		textlabel.setText(dropMessage.getDropPayload());

		PrettyTime p = new PrettyTime(resources.getLocale());
		dateLabel.setText(p.format(dropMessage.getCreationDate()));

		textlabel.setWrapText(true);
		dateLabel.setWrapText(true);

		textlabel.setTextAlignment(TextAlignment.JUSTIFY);
		dateLabel.setTextAlignment(TextAlignment.JUSTIFY);
		updateAvatar();
	}

	private void updateAvatar() {
		new AvatarView(e -> contact.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
	}
}
