package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
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


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textlabel.setText(dropMessage.getDropPayload());
		PrettyTime p = new PrettyTime(resources.getLocale());
		dateLabel.setText(p.format(dropMessage.getCreationDate()));

		textlabel.setWrapText(true);
		dateLabel.setWrapText(true);

		textlabel.setTextAlignment(TextAlignment.JUSTIFY);
		dateLabel.setTextAlignment(TextAlignment.JUSTIFY);

	}


}
