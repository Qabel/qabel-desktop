package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AccountingItemController extends AbstractController implements Initializable {

	ResourceBundle resourceBundle;

	@FXML
	Label alias;
	@FXML
	Label mail;

	@FXML
	Pane avatarContainer;

	@FXML
	RadioButton selectedRadio;

	@FXML
	public Node root;

	@Inject
	private Identity identity;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Inject
	private IdentityRepository identityRepository;

	TextInputDialog dialog;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

		alias.textProperty().addListener((o, a, b) -> updateAvatar());
		alias.setText(identity.getAlias());

		if (clientConfiguration.hasAccount()) {
			Account account = clientConfiguration.getAccount();
			mail.setText(account.getUser());
		}

		updateSelection();
		clientConfiguration.addObserver((o, arg) -> updateSelection());
	}

	private void updateAvatar() {
		new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
	}

	private void updateSelection() {
		selectedRadio.setSelected(identity.equals(clientConfiguration.getSelectedIdentity()));
	}

	public Identity getIdentity() {
		return identity;
	}

	public void edit(ActionEvent actionEvent) {
		dialog = new TextInputDialog(identity.getAlias());
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("accountingItemChangeAlias"));
		dialog.setContentText(resourceBundle.getString("accountingItemNewAlias"));
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::setAlias);
	}

	protected void setAlias(String alias) {
		identity.setAlias(alias);
		try {
			identityRepository.save(identity);
			this.alias.setText(alias);
		} catch (PersistenceException e) {
			alert("Failed to save identity", e);
		}
	}

	public void selectIdentity(ActionEvent actionEvent) {
		if (selectedRadio.isSelected()) {
			clientConfiguration.selectIdentity(identity);
		}
	}
}
