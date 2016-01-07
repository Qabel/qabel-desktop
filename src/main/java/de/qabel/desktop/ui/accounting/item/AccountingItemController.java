package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AccountingItemController extends AbstractController implements Initializable {
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

	@Inject
	private IdentityRepository identityRepository;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		alias.setText(identity.getAlias());
		mail.setText(account.getUser());
		provider.setText(account.getProvider());
	}

	public Identity getIdentity() {
		return identity;
	}

	public void edit(ActionEvent actionEvent) {
		TextInputDialog dialog = new TextInputDialog(identity.getAlias());
		dialog.setHeaderText(null);
		dialog.setTitle("Change Alias");
		dialog.setContentText("Please specify an alias for your new Identity");
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
}
