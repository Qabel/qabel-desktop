package de.qabel.desktop.ui.contact.item;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ContactItemController extends AbstractController implements Initializable {

	ResourceBundle resourceBundle;

	@FXML
	Label alias;
	@FXML
	Label email;
	@FXML
	Pane avatarContainer;

	@Inject
	private Contact contact;
	@Inject
	private ClientConfiguration clientConfiguration;
	@Inject
	private IdentityRepository identityRepository;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
		alias.setText(contact.getAlias());
		email.setText(contact.getEmail());
		updateAvatar();
	}

	private void updateAvatar() {
			new AvatarView(e -> contact.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
	}
}
