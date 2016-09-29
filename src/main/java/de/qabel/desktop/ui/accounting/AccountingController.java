package de.qabel.desktop.ui.accounting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.core.config.*;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.EntityExistsException;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import de.qabel.desktop.ui.accounting.item.DummyAccountingItemView;
import de.qabel.desktop.ui.accounting.wizard.WizardController;
import de.qabel.desktop.ui.accounting.wizard.WizardView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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
    Button addIdentity;

    @FXML
    Button importIdentity;

    @FXML
    Button exportIdentity;

    @FXML
    Button exportContact;

    @Inject
    Pane layoutWindow;

    List<AccountingItemView> itemViews = new LinkedList<>();
    ResourceBundle resourceBundle;

    ImageView imageView;

    WizardView wizardView;
    WizardController wizardController;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private IdentityBuilderFactory identityBuilderFactory;

    @Inject
    ClientConfig clientConfiguration;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            gson = buildGson();
        } catch (EntityNotFoundException | PersistenceException e) {
            alert(e);
        }
        resourceBundle = resources;

        loadIdentities();
        updateIdentityState();
        updateButtonIcons();
        clientConfiguration.onSelectIdentity(identity -> updateIdentityState());
        identityRepository.attach(() -> Platform.runLater(() -> loadIdentities()));
    }

    private void updateIdentityState() {
        Identity identity = clientConfiguration.getSelectedIdentity();
        exportIdentity.setDisable(identity == null);
        exportContact.setDisable(identity == null);
    }

    private void updateButtonIcons() {
        addIdentity.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/account.png"))));
        importIdentity.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/import_black.png"))));
        exportIdentity.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/export.png"))));
        exportContact.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/account_multiple.png"))));
    }

    private ImageView getImage(Image image) {
        imageView = new ImageView();
        imageView.setImage(image);
        return imageView;
    }

    private void initializeWizard() {
        wizardView = new WizardView();
        wizardView.getView(layoutWindow.getChildren()::add);
        wizardController = (WizardController) wizardView.getPresenter();
    }

    public void addIdentity() {
        initializeWizard();
        Platform.runLater(() -> wizardController.showPopup());
    }

    @FXML
    protected void handleImportIdentityButtonAction() throws URISyntaxException, QblDropInvalidURL {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resourceBundle.getString("accountingImportIdentity"));
        FileChooser.ExtensionFilter qidExtensionFilter = new FileChooser.ExtensionFilter(resourceBundle.getString("qidExtensionFilterLabel"), "*.qid");
        chooser.getExtensionFilters().add(qidExtensionFilter);
        File file = chooser.showOpenDialog(identityList.getScene().getWindow());
        try {
            importIdentity(file);
            loadIdentities();
        } catch (EntityExistsException e) {
            new Alert(Alert.AlertType.INFORMATION, resourceBundle.getString("identityExists")).show();
        } catch (IOException | PersistenceException | JSONException e) {
            alert(resourceBundle.getString("alertImportIdentityFail"), e);
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    protected void handleExportIdentityButtonAction(ActionEvent event) {

        Identity i = clientConfiguration.getSelectedIdentity();
        File file = createSaveFileChooser(resourceBundle.getString("accountingExportIdentity"), i.getAlias() + ".qid");
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
        File file = createSaveFileChooser(resourceBundle.getString("accountingExportContact"), i.getAlias() + ".qco");
        try {
            exportContact(i, file);
        } catch (IOException | QblStorageException e) {
            alert("Export contact fail", e);
        } catch (NullPointerException ignored) {
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

    private File createSaveFileChooser(String title, String defaultName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(defaultName);
        return chooser.showSaveDialog(identityList.getScene().getWindow());
    }

    Gson buildGson() throws EntityNotFoundException, PersistenceException {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapter(Contacts.class, new ContactsTypeAdapter(identityRepository.findAll()));
        builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
        builder.registerTypeAdapter(Identities.class, new IdentitiesTypeAdapter());
        return builder.create();
    }

}
