package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.connector.DropConnector;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class RemoteFileDetailsController extends AbstractController implements Initializable {
	@Inject
	private ContactRepository contactRepository;

	@Inject
	private ClientConfiguration clientConfiguration;

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

	TextInputDialog dialog;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		clientConfiguration.addObserver((o, arg) -> {
			if (arg instanceof Identity) {
				updateContacts();
			}
		});
		shareReceiver.setCellFactory(contactAliasCellFactory());
		shareReceiver.setConverter(contactAutocomleteResultConverter());

		shareReceiver.getSelectionModel().selectedItemProperty().addListener(showShareMessageDialog());
		shareReceiver.getEditor().textProperty().addListener(filterContacts());

		updateContacts();
		loadShares();
	}

	private void loadShares() {
		ObservableList<Node> shares = currentShares.getChildren();
		shares.clear();
		try {
			Contacts contacts = contactRepository.findContactsFromOneIdentity(clientConfiguration.getSelectedIdentity());
			for (BoxShare share : navigation.getSharesOf(boxObject)) {
				String recipientKeyId = share.getRecipient();
				shares.add(new Label(contacts.getByKeyIdentifier(recipientKeyId).getAlias()));
			}
		} catch (Exception e) {
			alert(e);
		}
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
				} catch (EntityNotFoundExcepion e) {
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
			dialog.setHeaderText("You are sharing the file " + boxObject.getName() + " with user " + newValue.getAlias() + ". Please insert a message for him/her.");
			dialog.showAndWait().ifPresent(message -> {
				share(newValue, message);
			});
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
		try {
			sharingService.shareAndSendMessage(clientConfiguration.getSelectedIdentity(), contact, (BoxFile)boxObject, message, navigation);
			if (navigation instanceof CachedBoxNavigation) {
				((CachedBoxNavigation) navigation).refresh();
				loadShares();
			}
		} catch (Exception e) {
			alert(e);
		}
	}

	private void updateContacts() {
		shareReceiver.getItems().clear();
		try {
			for (Contact c : getContacts().getContacts()) {
				shareReceiver.getItems().add(c);
			}
		} catch (Exception e) {
			alert(e);
		}
	}

	private Contacts getContacts() throws EntityNotFoundExcepion {
		return contactRepository.findContactsFromOneIdentity(clientConfiguration.getSelectedIdentity());
	}
}
