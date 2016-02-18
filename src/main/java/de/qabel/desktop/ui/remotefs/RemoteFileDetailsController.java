package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.BoxExternalReference;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxObject;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.connector.Connector;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import java.net.URL;
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

	@FXML
	ListView<Contact> shareReceiverList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		clientConfiguration.addObserver((o, arg) -> {
			if (arg instanceof Identity) {
				updateContacts();
			}
		});

		shareReceiverList.setCellFactory(param -> {
			ListCell<Contact> cell = new ListCell<>();
			cell.itemProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue != null) {
					cell.setText(newValue.getAlias());
				}
			});
			return cell;
		});

		shareReceiverList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Share message");
			dialog.setHeaderText("You are sharing the file " + boxObject.getName() + " with user " + newValue.getAlias() + ". Please insert a message for him/her.");
			dialog.showAndWait().ifPresent(message -> {
				share(newValue, message);
			});
		});

		updateContacts();
	}

	private void share(Contact contact, String message) {
		try {
			shareAndSendMessage(clientConfiguration.getSelectedIdentity(), contact, (BoxFile)boxObject, message, navigation);
		} catch (Exception e) {
			alert(e);
		}
	}

	@Inject
	Connector httpDropConnector;

	@Inject
	DropMessageRepository dropMessageRepository;

	private void shareAndSendMessage(Identity sender, Contact receiver, BoxFile objectToShare, String message, BoxNavigation navigation) throws QblStorageException, PersistenceException, QblNetworkInvalidResponseException {
		QblECPublicKey owner = sender.getEcPublicKey();
		BoxExternalReference ref = navigation.createFileMetadata(owner, objectToShare);
		ShareNotificationMessage share = new ShareNotificationMessage(ref.url, Hex.toHexString(ref.key), message);
		System.out.println(share.toJson());
		DropMessage dropMessage = new DropMessage(sender, share.toJson(), DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION);
		httpDropConnector.send(receiver, dropMessage);
		dropMessageRepository.addMessage(
				dropMessage,
				sender,
				receiver,
				false
		);
	}

	private void updateContacts() {
		shareReceiverList.getItems().clear();
		try {
			for (Contact c : contactRepository.findContactsFromOneIdentity(clientConfiguration.getSelectedIdentity()).getContacts()) {
				shareReceiverList.getItems().add(c);
			}
		} catch (Exception e) {
			alert(e);
		}
	}
}
