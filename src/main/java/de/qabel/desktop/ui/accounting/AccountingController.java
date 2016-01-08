package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class AccountingController extends AbstractController implements Initializable {
	private Identity selectedIdentity;

	@FXML
	VBox identityList;

	List<AccountingItemView> itemViews = new LinkedList<>();

	TextInputDialog dialog;

	@Inject
	private IdentityRepository identityRepository;

	@Inject
	private IdentityBuilderFactory identityBuilderFactory;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadIdentities();
	}

	private void loadIdentities() {
		try {
			identityList.getChildren().clear();
			for (Identity identity : identityRepository.findAll()) {
				final Map<String, Object> injectionContext = new HashMap<>();
				injectionContext.put("identity", identity);
				AccountingItemView itemView = new AccountingItemView(injectionContext::get);
				identityList.getChildren().add(itemView.getView());
				itemViews.add(itemView);
			}

		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void addIdentity(ActionEvent actionEvent) {
		addIdentity();
	}

	public void addIdentity() {
		dialog = new TextInputDialog("My Name");
		dialog.setHeaderText(null);
		dialog.setTitle("New Identity");
		dialog.setContentText("Please specify an alias for your new Identity");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::addIdentityWithAlias);
	}

	protected void addIdentityWithAlias(String alias) {
		Identity identity = identityBuilderFactory.factory().withAlias(alias).build();
		try {
			identityRepository.save(identity);
		} catch (PersistenceException e) {
			alert("Failed to save new identity", e);
		}
		loadIdentities();
	}
}
