package de.qabel.desktop.ui.contact.menu;


import de.qabel.core.config.Contact;
import de.qabel.core.config.ContactExportImport;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.json.JSONException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ContactMenuController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    private ContactRepository contactRepository;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private Pane layoutWindow;

    @Inject
    private TransactionManager transactionManager;

    @FXML
    AnchorPane menuContact;

    @FXML
    VBox vboxMenu;

    @FXML
    Button importButton;

    @FXML
    Button exportToFileButton;

    @FXML
    Button importFromFile;
    @FXML
    Button importFromQR;
    @FXML
    Button searchButton;
    @FXML
    Button enterContact;
    @FXML
    Button exportContactsToFile;
    @FXML
    Button exportContactsToQR;

    Identity identity;

    ImageView imageView;

    public PopOver popOver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }

    public void showMenu(double coordX, double coordY) {
        identity = clientConfiguration.getSelectedIdentity();
        initializePopOver();
        popOver.show(menuContact, coordX, coordY);
        createButtonGraphics();
    }

    private void createButtonGraphics() {
        Tooltip.install(importButton, new Tooltip(resourceBundle.getString("contactImport")));
        Tooltip.install(exportToFileButton, new Tooltip(resourceBundle.getString("contactExport")));
        importFromFile.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/import_black.png"))));
        importFromQR.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/qrcode.png"))));
        searchButton.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/cloud_search.png"))));
        enterContact.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/pencil.png"))));
        exportContactsToFile.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/export.png"))));
        exportContactsToQR.setGraphic(getImage(new Image(getClass().getResourceAsStream("/img/qrcode.png"))));
    }

    private ImageView getImage(Image image) {
        imageView = new ImageView();
        imageView.setImage(image);
        return imageView;
    }

    private void initializePopOver() {
        popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setContentNode(new VBox(vboxMenu));
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
        popOver.setOnHiding(event -> layoutWindow.getChildren().remove(menuContact));
    }

    public void handleExportContactsButtonAction() throws EntityNotFoundException, IOException, JSONException {
        tryOrAlert(() -> {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle(resourceBundle.getString("contactDownload"));
                chooser.setInitialFileName("Contacts.qco");
                File file = chooser.showSaveDialog(menuContact.getScene().getWindow());
                exportContacts(file);
            } catch (NullPointerException ignored) {
            }
        });
    }

    void exportContacts(File file) throws EntityNotFoundException, IOException, JSONException, PersistenceException {
        Contacts contacts = contactRepository.find(identity);
        String jsonContacts = ContactExportImport.exportContacts(contacts);
        writeStringInFile(jsonContacts, file);
    }

    public void handleImportContactsButtonAction() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resourceBundle.getString("contactDownloadFolder"));
        FileChooser.ExtensionFilter qcoExtensionFilter = new FileChooser.ExtensionFilter(resourceBundle.getString("qcoExtensionFilterLabel"), "*.qco");
        chooser.getExtensionFilters().add(qcoExtensionFilter);
        File file = chooser.showOpenDialog(menuContact.getScene().getWindow());
        try {
            importContacts(file);
        } catch (IOException | PersistenceException | JSONException e) {
            alert(resourceBundle.getString("alertImportContactFail"), e);
        } catch (NullPointerException ignored) {
        } catch (QblDropInvalidURL qblDropInvalidURL) {
            qblDropInvalidURL.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void importContacts(File file) throws IOException, URISyntaxException, QblDropInvalidURL, PersistenceException, JSONException {
        String content = readFile(file);
        identity = clientConfiguration.getSelectedIdentity();
        transactionManager.transactional(() -> {
            try {
                Contacts contacts = ContactExportImport.parseContactsForIdentity(identity, content);
                for (Contact c : contacts.getContacts()) {
                    contactRepository.save(c, identity);
                }
            } catch (Exception ignore) {
                Contact c = ContactExportImport.parseContactForIdentity(content);
                contactRepository.save(c, identity);
            }
        });
    }
}
