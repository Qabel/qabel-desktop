package de.qabel.desktop.ui.actionlog.item;

import de.qabel.core.config.Identity;
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
	private String text;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textlabel.setText(text);
		textlabel.setWrapText(true);
		textlabel.setTextAlignment(TextAlignment.JUSTIFY);

	}


}
