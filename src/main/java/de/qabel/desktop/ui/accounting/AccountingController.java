package de.qabel.desktop.ui.accounting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import de.qabel.desktop.ui.accounting.item.DummyAccountingItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.json.JSONException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class AccountingController extends AbstractController implements Initializable {
	private Identity selectedIdentity;

	@FXML
	VBox identityList;

	@FXML
	Button importIdentity;

	@FXML
	Button exportIdentity;

	@FXML
	Button exportContact;

	List<AccountingItemView> itemViews = new LinkedList<>();
	TextInputDialog dialog;
	ResourceBundle resourceBundle;

	@Inject
	private IdentityRepository identityRepository;

	@Inject
	private IdentityBuilderFactory identityBuilderFactory;

	@Inject
	ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadIdentities();
		try {
			gson = buildGson();
		} catch (EntityNotFoundExcepion | PersistenceException e) {
			alert(e);
		}
		this.resourceBundle = resources;

		updateIdentityState();
		clientConfiguration.addObserver((o, arg) -> {
			if (arg instanceof Identity) {
				updateIdentityState();
			}
		});
	}

	private void updateIdentityState() {
		Identity identity = clientConfiguration.getSelectedIdentity();

		exportIdentity.setDisable(identity == null);
		exportContact.setDisable(identity == null);
	}

	public void addIdentity(ActionEvent actionEvent) {
		addIdentity();
	}

	public void addIdentity() {
		dialog = new TextInputDialog(resourceBundle.getString("accountingNewIdentity"));
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("accountingNewIdentity"));
		dialog.setContentText(resourceBundle.getString("accountingNewIdentity"));
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::addIdentityWithAlias);
	}

	@FXML
	protected void handleImportIdentityButtonAction(ActionEvent event) throws URISyntaxException, QblDropInvalidURL {

		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("accountingDownloadFolder"));
		File file = chooser.showOpenDialog(identityList.getScene().getWindow());
		try {
			importIdentity(file);
			loadIdentities();
		} catch (IOException | PersistenceException | JSONException e) {
			alert("Import identity fail", e);
		} catch (NullPointerException ignored) {
		}
	}

	@FXML
	protected void handleExportIdentityButtonAction(ActionEvent event) {

		Identity i = clientConfiguration.getSelectedIdentity();
		File file = createSaveFileChooser(i.getAlias() + ".qid");
		try {
			exportIdentity(i, file);
			loadIdentities();
		} catch (IOException | QblStorageException e) {
			alert("Export identity fail", e);
		} catch (NullPointerException ignored) {
		}
	}

	@FXML
	protected void handleExportContactButtonAction(ActionEvent event) {
		Identity i = clientConfiguration.getSelectedIdentity();
		File file = createSaveFileChooser(i.getAlias() + ".qco");
		try {
			exportContact(i, file);
		} catch (IOException | QblStorageException e) {
			alert("Export contact fail", e);
		} catch (NullPointerException ignored) {
		}
	}

	protected void addIdentityWithAlias(String alias) {
		Identity identity = identityBuilderFactory.factory().withAlias(alias).build();
		try {
			identityRepository.save(identity);
		} catch (PersistenceException e) {
			alert("Failed to save new identity", e);
		}
		loadIdentities();
		if (clientConfiguration.getSelectedIdentity() == null) {
			clientConfiguration.selectIdentity(identity);
		}
	}

	void importIdentity(File file) throws IOException, PersistenceException, URISyntaxException, QblDropInvalidURL, JSONException {
		String content = readFile(file);
		Identity i = IdentityExportImport.parseIdentity(content);
		identityRepository.save(i);
	}

	void exportIdentity(Identity i, File file) throws IOException, QblStorageException {
		String json = IdentityExportImport.exportIdentity(i);
		writeStringInFile(json, file);
	}

	void exportContact(Identity i, File file) throws IOException, QblStorageException {
		String json = ContactExportImport.exportIdentityAsContact(i);
		writeStringInFile(json, file);
	}

	ResourceBundle getRessource() {
		return resourceBundle;
	}

	private void loadIdentities() {
		try {
			identityList.getChildren().clear();
			Identities identities = identityRepository.findAll();

			if (identities.getIdentities().size() == 0) {
				DummyAccountingItemView itemView = new DummyAccountingItemView();
				identityList.getChildren().add(itemView.getView());
				return;
			}


			for (Identity identity : identities.getIdentities()) {
				final Map<String, Object> injectionContext = new HashMap<>();
				injectionContext.put("identity", identity);
				AccountingItemView itemView = new AccountingItemView(injectionContext::get);
				identityList.getChildren().add(itemView.getView());
				itemViews.add(itemView);
			}
		} catch (Exception e) {
			alert("Failed to load identities", e);
		}

	}

	private File createSaveFileChooser(String defaultName) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("accountingExport"));
		chooser.setInitialFileName(defaultName);
		return chooser.showSaveDialog(identityList.getScene().getWindow());
	}

	Gson buildGson() throws EntityNotFoundExcepion, PersistenceException {
		final GsonBuilder builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		builder.registerTypeAdapter(Contacts.class, new ContactsTypeAdapter(identityRepository.findAll()));
		builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
		builder.registerTypeAdapter(Identities.class, new IdentitiesTypeAdapter());
		return builder.create();
	}

}
