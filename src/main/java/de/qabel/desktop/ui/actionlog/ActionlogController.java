package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.AbstractBinaryDropMessage;
import de.qabel.core.crypto.BinaryDropMessageV0;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblSpoofedSenderException;
import de.qabel.core.exceptions.QblVersionMismatchException;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;


public class ActionlogController extends AbstractController implements Initializable {

	List<ActionlogItemView> messageView = new LinkedList<>();

	@FXML
	VBox messages;

	@FXML
	ScrollPane scroller;

	@FXML
	TextArea textarea;

	@Inject
	ClientConfiguration clientConfiguration;

	@Inject
	private ContactRepository contactRepository;
	@Inject
	private DropMessageRepository dropMessageRepository;

	Identity identity;
	Contact c;
	Date lastDate;
	DropHTTP dHTTP = new DropHTTP();

	public void initialize(URL location, ResourceBundle resources) {

		identity = clientConfiguration.getSelectedIdentity();
		c = new Contact(identity, identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
		loadMessages(c);

		scroller.setVvalue(scroller.getVmax());
		addListener();
	}

	private void addListener() {
		((Region) scroller.getContent()).heightProperty().addListener((ov, old_val, new_val) -> {
			if (scroller.getVvalue() != scroller.getVmax()) {
				scroller.setVvalue(scroller.getVmax());
			}
		});
		textarea.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isControlDown()) {

				try {
					sendDropMessage(c, textarea.getText());
					receiveDropMessages(lastDate);
					loadMessages(c);

				} catch (QblDropPayloadSizeException | PersistenceException | QblDropInvalidMessageSizeException | QblVersionMismatchException | QblSpoofedSenderException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@FXML
	protected void handleSubmitButtonAction(ActionEvent event) throws QblDropPayloadSizeException, EntityNotFoundExcepion, PersistenceException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException {
		if (textarea.getText() == "") {
			return;
		}
		sendDropMessage(c, textarea.getText());
		receiveDropMessages(lastDate);

		loadMessages(c);
	}

	void receiveDropMessages(Date siceDate) throws QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, PersistenceException {
		List<DropMessage> dropMessages = getDropMassages(siceDate);

		if (dropMessages == null) {
			return;
		}

		for (DropMessage d : dropMessages) {
			Contact contact = findSender(d);

			if (lastDate.getTime() < d.getCreationDate().getTime()) {
				lastDate = d.getCreationDate();
				dropMessageRepository.addMessage(d, contact, false);
				loadMessages(c);
			}
		}
	}

	void addMessageToActionlog(DropMessage dropMessage) {
		lastDate = dropMessage.getCreationDate();
		Map<String, Object> injectionContext = new HashMap<>();
		Contact sender = findSender(dropMessage);
		injectionContext.put("dropMessage", dropMessage);
		injectionContext.put("contact", sender);
		OtherActionlogItemView otherItemView = new OtherActionlogItemView(injectionContext::get);
		messages.getChildren().add(otherItemView.getView());
		messageView.add(otherItemView);
	}

	void addOwnMessageToActionlog(DropMessage dropMessage) {

		if (dropMessage.getDropPayload() == null || dropMessage.getDropPayload().equals("")) {
			return;
		}
		Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("dropMessage", dropMessage);
		MyActionlogItemView myItemView = new MyActionlogItemView(injectionContext::get);
		messages.getChildren().add(myItemView.getView());
		messageView.add(myItemView);
		textarea.setText("");
	}

	private Contact findSender(DropMessage dropMessage) {

		List<Contact> contactList = null;

		try {
			contactList = contactRepository.findAllContactFromOneIdentity(identity);
		} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
			entityNotFoundExcepion.printStackTrace();
		}

		for (Contact c : contactList) {
			if (dropMessage.getSenderKeyId().equals(c.getEcPublicKey().getReadableKeyIdentifier())) {
				return c;
			}
		}

		return null;
	}

	DropMessage sendDropMessage(final Contact c, String text) throws QblDropPayloadSizeException, PersistenceException {


		if (text == null || text.equals("")) {
			return null;
		}
		DropMessage d = new DropMessage(identity, text, "dropMessage");
		final BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(d);
		final byte[] messageByteArray = binaryMessage.assembleMessageFor(c);
		DropURL dropURL = convertCollectionIntoDropUrl(c.getDropUrls());

		HTTPResult<?> dropResult = dHTTP.send(dropURL.getUri(), messageByteArray);

		if (dropResult.getResponseCode() != 200) {
			return null;
		}
		dropMessageRepository.addMessage(d, c, true);

		return d;
	}


	void loadMessages(Contact c) {
		try {
			messages.getChildren().clear();
			List<PersitsDropMessage> dropMessages = dropMessageRepository.loadConversation(c);
			for (PersitsDropMessage d : dropMessages) {

				if (lastDate == null || lastDate.getTime() < d.getDropMessage().getCreationDate().getTime()) {
					lastDate = d.getDropMessage().getCreationDate();
				}

				if (d.getSend()) {
					addOwnMessageToActionlog(d.getDropMessage());
				} else {
					addMessageToActionlog(d.getDropMessage());
				}
			}


		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	private DropURL convertCollectionIntoDropUrl(Set<DropURL> dropUrls) {
		for (DropURL d : dropUrls) {
			return d;
		}
		return null;
	}

	List<DropMessage> getDropMassages(Date siceDate) throws QblVersionMismatchException, QblDropInvalidMessageSizeException, QblSpoofedSenderException {
		DropURL d = convertCollectionIntoDropUrl(identity.getDropUrls());
		LinkedList<DropMessage> dropMessages = new LinkedList<>();

		if (d == null) {
			return null;
		}

		HTTPResult<Collection<byte[]>> result = receiveMessages(siceDate, d);
		createDropMessagesFromHttpResult(dropMessages, result);

		return dropMessages;
	}

	private void createDropMessagesFromHttpResult(LinkedList<DropMessage> dropMessages, HTTPResult<Collection<byte[]>> result) throws QblVersionMismatchException, QblDropInvalidMessageSizeException, QblSpoofedSenderException {
		for (byte[] message : result.getData()) {
			AbstractBinaryDropMessage binMessage;
			byte binaryFormatVersion = message[0];

			if (binaryFormatVersion != 0) {
				continue;
			}

			binMessage = new BinaryDropMessageV0(message);
			DropMessage dropMessage = binMessage.disassembleMessage(identity);
			if (dropMessage != null) {
				dropMessages.add(dropMessage);
			}
		}
	}

	private HTTPResult<Collection<byte[]>> receiveMessages(Date siceDate, DropURL d) {
		HTTPResult<Collection<byte[]>> result;
		if (siceDate == null) {
			result = dHTTP.receiveMessages(d.getUri());
		} else {
			result = dHTTP.receiveMessages(d.getUri(), siceDate.getTime() - 1);
		}
		return result;
	}
}
