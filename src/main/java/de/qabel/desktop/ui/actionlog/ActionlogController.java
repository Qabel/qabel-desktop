package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.PersistenceClientConfigurationRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.connector.Connector;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;


public class ActionlogController extends AbstractController implements Initializable, Observer {

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
	@Inject
	Connector httpDropConnector;

	Identity identity;
	Contact c;
	Date lastUpdate;

	public void initialize(URL location, ResourceBundle resources) {
		lastUpdate = clientConfiguration.getLastUpdate();
		identity = clientConfiguration.getSelectedIdentity();
		c = new Contact(identity, identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
		try {
			loadMessages(c);
		} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
			entityNotFoundExcepion.printStackTrace();
		}
		dropMessageRepository.addObserver(this);
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
				} catch (QblDropPayloadSizeException | QblNetworkInvalidResponseException | PersistenceException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@FXML
	protected void handleSubmitButtonAction(ActionEvent event) throws QblDropPayloadSizeException, EntityNotFoundExcepion, PersistenceException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, QblNetworkInvalidResponseException {
		if (textarea.getText().equals("")) {
			return;
		}
		sendDropMessage(c, textarea.getText());
		textarea.setText("");
	}

	void sendDropMessage(Contact c, String text) throws QblDropPayloadSizeException, QblNetworkInvalidResponseException, PersistenceException {
		DropMessage d = new DropMessage(identity, text, "dropMessage");
		dropMessageRepository.addMessage(d, c, true);
		httpDropConnector.send(c, d);
	}

	void loadMessages(Contact c) throws EntityNotFoundExcepion {
		try {
			messages.getChildren().clear();
			List<PersistenceDropMessage> dropMessages = dropMessageRepository.loadConversation(c);
			for (PersistenceDropMessage d : dropMessages) {

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
		lastUpdate = dropMessage.getCreationDate();
		Map<String, Object> injectionContext = new HashMap<>();
		Contact sender = contactRepository.findByKeyId(identity, dropMessage.getSenderKeyId());
		injectionContext.put("dropMessage", dropMessage);
		injectionContext.put("contact", sender);
		OtherActionlogItemView otherItemView = new OtherActionlogItemView(injectionContext::get);
		messages.getChildren().add(otherItemView.getView());
		messageView.add(otherItemView);
	}

	void addOwnMessageToActionlog(DropMessage dropMessage) {

		if (dropMessage.getDropPayload().equals("")) {
			return;
		}
		Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("dropMessage", dropMessage);
		MyActionlogItemView myItemView = new MyActionlogItemView(injectionContext::get);
		messages.getChildren().add(myItemView.getView());
		messageView.add(myItemView);
	}

	void setText(String text) {
		this.textarea.setText(text);
	}

	@Override
	public void update(Observable o, Object arg) {

		Platform.runLater(() -> {
			try {
				loadMessages(c);
			} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
				entityNotFoundExcepion.printStackTrace();
			}
		});
	}
}
