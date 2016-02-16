package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadIdentities();
		buildGson();
		this.resourceBundle = resources;

		updateIdentityState();
		clientConfiguration.addObserver((o, arg) -> { if (arg instanceof Identity) {updateIdentityState();}});
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
		dialog = new TextInputDialog(resourceBundle.getString("newIdentity"));
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("newIdentity"));
		dialog.setContentText(resourceBundle.getString("newIdentity"));
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::addIdentityWithAlias);
	}

	@FXML
	protected void handleImportIdentityButtonAction(ActionEvent event) throws URISyntaxException, QblDropInvalidURL {

		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("downloadFolder"));
		File file = chooser.showOpenDialog(identityList.getScene().getWindow());
		try {
			importIdentity(file);
			loadIdentities();
		} catch (IOException | PersistenceException e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void handleExportIdentityButtonAction(ActionEvent event) {

		Identity i = clientConfiguration.getSelectedIdentity();
		File file = createSaveFileChooser(i.getAlias() + "_Identity.json");
		try {
			exportIdentity(i, file);
			loadIdentities();
		} catch (IOException | QblStorageException e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void handleExportContactButtonAction(ActionEvent event) {
		Identity i = clientConfiguration.getSelectedIdentity();
		File file = createSaveFileChooser(i.getAlias() + "_Contact.json");
		try {
			exportContact(i, file);
		} catch (IOException | QblStorageException e) {
			e.printStackTrace();
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

	void importIdentity(File file) throws IOException, PersistenceException, URISyntaxException, QblDropInvalidURL {
		String content = readFile(file);
		GsonIdentity gi = gson.fromJson(content, GsonIdentity.class);
		Identity i = gsonIdentityToIdentiy(gi);
		identityRepository.save(i);
	}

	void exportIdentity(Identity i, File file) throws IOException, QblStorageException {
		GsonIdentity gi = new GsonIdentity().fromIdentity(i);
		String json = gson.toJson(gi);
		writeStringInFile(json, file);
	}

	void exportContact(Identity i, File file) throws IOException, QblStorageException {
		GsonContact gc = new GsonContact().fromEntity(i);
		String json = gson.toJson(gc);
		writeStringInFile(json, file);
	}


	ResourceBundle getRessource(){
		return resourceBundle;
	}


	Identity gsonIdentityToIdentiy(GsonIdentity gi) throws URISyntaxException, QblDropInvalidURL {

		ArrayList<DropURL> collection = generateDropURLs(gi.getDropUrls());
		QblECKeyPair qblECKeyPair = new QblECKeyPair(gi.getPrivateKey());

		return new Identity(gi.getAlias(), collection, qblECKeyPair);
	}

	private ArrayList<DropURL> generateDropURLs(List<String> drops) throws URISyntaxException, QblDropInvalidURL {
		ArrayList<DropURL> collection = new ArrayList<>();

		for (String uri : drops) {
			DropURL dropURL = new DropURL(uri);
			collection.add(dropURL);
		}
		return collection;
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

	private File createSaveFileChooser(String defaultName) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export");
		chooser.setInitialFileName(defaultName);
		return chooser.showSaveDialog(identityList.getScene().getWindow());
	}
}
