package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class MyActionlogItemController extends AbstractController implements Initializable, ActionlogItem {

	ResourceBundle resourceBundle;

	@FXML
	Label textlabel;

	@Inject
	private DropMessage dropMessage;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textlabel.setText(dropMessage.getDropPayload() + " - " + dropMessage.getCreationDate().getTime());
		textlabel.setWrapText(true);
		textlabel.setTextAlignment(TextAlignment.JUSTIFY);

	}


}
