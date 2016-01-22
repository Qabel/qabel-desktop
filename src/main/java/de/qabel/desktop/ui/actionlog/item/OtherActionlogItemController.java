package de.qabel.desktop.ui.actionlog.item;

import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class OtherActionlogItemController extends AbstractController implements Initializable, ActionlogItem {

	ResourceBundle resourceBundle;

	@FXML
	Label textlabel;
	@FXML
	Pane avatarContainer;

	@Inject
	OtherTextWrapper wrapper;



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textlabel.setText(wrapper.getText());
		textlabel.setWrapText(true);
		textlabel.setTextAlignment(TextAlignment.JUSTIFY);
		updateAvatar();
	}

	private void updateAvatar() {
		new AvatarView(e -> wrapper.getContact().getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
	}


}
