package de.qabel.desktop.ui.accounting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.*;
import de.qabel.core.event.EventSource;
import de.qabel.core.event.identity.IdentitiesChangedEvent;
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
import java.util.concurrent.TimeUnit;

public class AccountingController extends AbstractController implements Initializable {
    @FXML
    VBox identityList;

    @FXML
    Button addIdentity;

    @FXML
    Button importIdentity;

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
    ClientConfig clientConfiguration;

    @Inject
    private EventSource eventSource;

    @Inject
    private int debounceTimeout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            gson = buildGson();
        } catch (EntityNotFoundException | PersistenceException e) {
            alert(e);
        }
        resourceBundle = resources;

        loadIdentities();
        updateButtonIcons();

        eventSource.events(IdentitiesChangedEvent.class)
            .debounce(debounceTimeout, TimeUnit.MILLISECONDS)
            .subscribe(e -> Platform.runLater(this::loadIdentities));

        identityRepository.attach(() -> Platform.runLater(this::loadIdentities));
    }

    private void updateButtonIcons() {
        addIdentity.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/account.png"))));
        importIdentity.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/import_black.png"))));
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

    void importIdentity(File file) throws IOException, PersistenceException, URISyntaxException, QblDropInvalidURL, JSONException {
        String content = readFile(file);
        Identity i = IdentityExportImport.parseIdentity(content);
        identityRepository.save(i);
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

    Gson buildGson() throws EntityNotFoundException, PersistenceException {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapter(Contacts.class, new ContactsTypeAdapter(identityRepository.findAll()));
        builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
        builder.registerTypeAdapter(Identities.class, new IdentitiesTypeAdapter());
        return builder.create();
    }
}
