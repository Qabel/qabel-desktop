package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxObject;
import de.qabel.desktop.storage.BoxShare;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.inject.Inject;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class RemoteFileDetailsController extends AbstractController implements Initializable {
    @Inject
    private ContactRepository contactRepository;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private BoxObject boxObject;

    @Inject
    private BoxNavigation navigation;

    @Inject
    DropMessageRepository dropMessageRepository;

    @Inject
    SharingService sharingService;

    @FXML
    ComboBox<Contact> shareReceiver;

    @FXML
    VBox currentShares;

    @FXML
    private Node unshare;

    TextInputDialog dialog;
    Alert confirmationDialog;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        clientConfiguration.onSelectIdentity(i -> updateContacts());
        shareReceiver.setCellFactory(contactAliasCellFactory());
        shareReceiver.setConverter(contactAutocomleteResultConverter());

        shareReceiver.getSelectionModel().selectedItemProperty().addListener(showShareMessageDialog());
        shareReceiver.getEditor().textProperty().addListener(filterContacts());

        unshare.setOnMouseClicked(event -> askForUnshare());

        updateContacts();
        loadShares();
    }

    private void askForUnshare() {
        String question = getString(resources, "confirmUnshare", boxObject.getName());
        confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION, question, ButtonType.CANCEL, ButtonType.YES);
        confirmationDialog.setHeaderText(null);
        confirmationDialog.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.YES)
                .ifPresent(buttonType1 -> unshare());
    }

    private void unshare() {
        tryOrAlert(() -> {
            navigation.unshare(boxObject);
            loadShares();
        });
    }

    private void loadShares() {
        Platform.runLater(() -> {
            ObservableList<Node> shares = currentShares.getChildren();
            shares.clear();
            tryOrAlert(() -> {
                Contacts contacts = contactRepository.find(clientConfiguration.getSelectedIdentity());
                for (BoxShare share : navigation.getSharesOf(boxObject)) {
                    String recipientKeyId = share.getRecipient();
                    Contact contact = contacts.getByKeyIdentifier(recipientKeyId);
                    String alias = contact == null ? recipientKeyId : contact.getAlias();
                    shares.add(new Label(alias));
                }
            });
        });
    }

    private StringConverter<Contact> contactAutocomleteResultConverter() {
        return new StringConverter<Contact>() {
            @Override
            public String toString(Contact contact) {
                if (contact == null) {
                    return "?";
                }
                return contact.getAlias();
            }

            @Override
            public Contact fromString(String alias) {
                try {
                    for (Contact c : getContacts().getContacts()) {
                        if (c.getAlias().equals(alias)) {
                            return c;
                        }
                    }
                } catch (PersistenceException e) {
                    alert(e);
                }
                return shareReceiver.getItems().size() > 0 ? shareReceiver.getItems().get(0) : null;
            }
        };
    }

    private ChangeListener<String> filterContacts() {
        return (observable, oldValue, newValue) -> {
            Contact selectedItem = shareReceiver.getSelectionModel().getSelectedItem();
            if (newValue == null || selectedItem != null && newValue.equals(selectedItem.getAlias())) {
                return;
            }

            shareReceiver.hide();
            filter(newValue);
            shareReceiver.show();
        };
    }

    private ChangeListener<Contact> showShareMessageDialog() {
        return (observable, oldValue, newValue) -> {
            if (boxObject == null || newValue == null) {
                return;
            }
            dialog = new TextInputDialog();
            dialog.setTitle("Share message");
            String header = getString(resources, "remoteFileRemoteFileShare", boxObject.getName(), newValue.getAlias());
            dialog.setHeaderText(header);
            dialog.showAndWait().ifPresent(message -> share(newValue, message));
            dialog = null;
        };
    }

    private static Callback<ListView<Contact>, ListCell<Contact>> contactAliasCellFactory() {
        return param -> {
            ListCell<Contact> cell = new ListCell<>();
            cell.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    cell.setText(newValue.getAlias());
                }
            });
            return cell;
        };
    }

    private void filter(String text) {
        updateContacts();
        Iterator<Contact> iter = shareReceiver.getItems().iterator();
        while (iter.hasNext()) {
            Contact item = iter.next();
            if (!item.getAlias().toLowerCase().contains(text.toLowerCase())) {
                iter.remove();
            }
        }
        shareReceiver.setVisibleRowCount(Math.max(10, shareReceiver.getItems().size()));
    }

    private void share(Contact contact, String message) {
        tryOrAlert(() -> {
            sharingService.shareAndSendMessage(clientConfiguration.getSelectedIdentity(), contact, (BoxFile) boxObject, message, navigation);
            if (navigation instanceof CachedBoxNavigation) {
                ((CachedBoxNavigation) navigation).refresh();
                loadShares();
            }
        });
    }

    private void updateContacts() {
        shareReceiver.getItems().clear();
        tryOrAlert(() -> getContacts().getContacts().forEach(shareReceiver.getItems()::add));
    }

    private Contacts getContacts() throws PersistenceException {
        return contactRepository.find(clientConfiguration.getSelectedIdentity());
    }
}
