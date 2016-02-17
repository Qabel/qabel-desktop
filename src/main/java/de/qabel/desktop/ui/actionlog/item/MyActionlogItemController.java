package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import org.ocpsoft.prettytime.PrettyTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class MyActionlogItemController extends AbstractController implements Initializable, ActionlogItem {

	ResourceBundle resourceBundle;

	@FXML
	Label textlabel;
	@FXML
	Label dateLabel;

	@Inject
	private DropMessage dropMessage;
	PrettyTime p;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textlabel.setText(dropMessage.getDropPayload());
		p = new PrettyTime(resources.getLocale());
		dateLabel.setText(p.format(dropMessage.getCreationDate()));

		textlabel.setWrapText(true);
		dateLabel.setWrapText(true);

		textlabel.setTextAlignment(TextAlignment.JUSTIFY);
		dateLabel.setTextAlignment(TextAlignment.JUSTIFY);

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
