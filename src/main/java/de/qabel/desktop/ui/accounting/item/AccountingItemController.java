package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountingItemController implements Initializable {
	@FXML
	Label alias;
	@FXML
	Label mail;
	@FXML
	Label provider;

	public static ToggleGroup globalIdentityToggle = new ToggleGroup();

	public ToggleGroup identityToggle = globalIdentityToggle;

	@FXML
	public Node root;

	@Inject
	private Identity identity;

	@Inject
	private Account account;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		alias.setText(identity.getAlias());
		mail.setText(account.getUser());
		provider.setText(account.getProvider());
	}

	public Identity getIdentity() {
		return identity;
	}
}
