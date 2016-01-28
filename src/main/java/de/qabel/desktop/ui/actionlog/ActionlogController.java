package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
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

	Identity identity;
	Contact c;
	Date lastDate;

	public void initialize(URL location, ResourceBundle resources) {
		try {
			identity = clientConfiguration.getSelectedIdentity();
			c = new Contact(identity, identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
			receiveDropMessages();

			scroller.setVvalue(scroller.getVmax());
			addChangeListener();

		} catch (QblVersionMismatchException | QblDropInvalidMessageSizeException | QblSpoofedSenderException | EntityNotFoundExcepion entityNotFoundExcepion) {
			alert("initialize fail", entityNotFoundExcepion);
		}
	}

	private void addChangeListener() {
		((Region) scroller.getContent()).heightProperty().addListener((ov, old_val, new_val) -> {
			if (scroller.getVvalue() != scroller.getVmax()) {
				scroller.setVvalue(scroller.getVmax());
			}
		});
	}

	@FXML
	protected void handleSubmitButtonAction(ActionEvent event) throws QblDropPayloadSizeException, EntityNotFoundExcepion, QblNetworkInvalidResponseException {
		DropMessage d = sendDropMessage(c, textarea.getText());
		addOwnMessageToActionlog(d);
	}

	@FXML
	protected void handleReloadMessagesButtonAction(ActionEvent event) {
		try {
			receiveDropMessages();
		} catch (QblDropInvalidMessageSizeException | QblVersionMismatchException | QblSpoofedSenderException | EntityNotFoundExcepion e) {
			e.printStackTrace();
		}
	}

	void receiveDropMessages() throws QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, EntityNotFoundExcepion {
		HttpDropConnector connector = new HttpDropConnector();
		List<DropMessage> dropMessages = connector.getDropMessages(identity, lastDate);

		for (DropMessage d : dropMessages) {
			addMessageToActionlog(d);
			lastDate = d.getCreationDate();
		}
	}

	DropMessage sendDropMessage(final Contact c, String text) throws QblDropPayloadSizeException, QblNetworkInvalidResponseException {
		DropMessage d = new DropMessage(identity, text, "dropMessage");
		HttpDropConnector connector = new HttpDropConnector();
		connector.send(c,d);
		return d;
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
		Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("dropMessage", dropMessage);
		MyActionlogItemView myItemView = new MyActionlogItemView(injectionContext::get);
		messages.getChildren().add(myItemView.getView());
		messageView.add(myItemView);
		textarea.setText("");
	}
}
