package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.connector.HttpDropConnector;
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

	public void initialize(URL location, ResourceBundle resources) {

		identity = clientConfiguration.getSelectedIdentity();
		c = new Contact(identity, identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
		try {
			loadMessages(c);
		} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
			entityNotFoundExcepion.printStackTrace();
		}

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

				} catch (QblDropPayloadSizeException | PersistenceException | QblDropInvalidMessageSizeException | QblVersionMismatchException | QblSpoofedSenderException | QblNetworkInvalidResponseException | EntityNotFoundExcepion e) {
					e.printStackTrace();
				}
			}
		});
	}

	@FXML
	protected void handleSubmitButtonAction(ActionEvent event) throws QblDropPayloadSizeException, EntityNotFoundExcepion, PersistenceException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, QblNetworkInvalidResponseException {
		if (textarea.getText() == "") {
			return;
		}
		sendDropMessage(c, textarea.getText());
		receiveDropMessages(lastDate);
		loadMessages(c);
	}

	void receiveDropMessages(Date siceDate) throws QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, PersistenceException, EntityNotFoundExcepion {

		HttpDropConnector connector = new HttpDropConnector();
		List<DropMessage> dropMessages = connector.receive(identity, siceDate);

		for (DropMessage d : dropMessages) {
			Contact contact = contactRepository.findByKeyId(identity, d.getSenderKeyId());

			if (lastDate == null || lastDate.getTime() < d.getCreationDate().getTime()) {
				lastDate = d.getCreationDate();
				dropMessageRepository.addMessage(d, contact, false);
			}
		}
		loadMessages(c);
	}

	void sendDropMessage(Contact c, String text) throws QblDropPayloadSizeException, QblNetworkInvalidResponseException, PersistenceException {
		DropMessage d = new DropMessage(identity, text, "dropMessage");
		dropMessageRepository.addMessage(d, c, true);
		HttpDropConnector connector = new HttpDropConnector();
		connector.send(c, d);
	}

	void loadMessages(Contact c) throws EntityNotFoundExcepion {
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

	void addMessageToActionlog(DropMessage dropMessage) throws EntityNotFoundExcepion {
		lastDate = dropMessage.getCreationDate();
		Map<String, Object> injectionContext = new HashMap<>();
		Contact sender = contactRepository.findByKeyId(identity, dropMessage.getSenderKeyId());
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
}
