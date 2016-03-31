package de.qabel.desktop.ui.contact.item;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.Indicator;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.item.SelectionEvent;
import de.qabel.desktop.ui.actionlog.Actionlog;
import de.qabel.desktop.ui.actionlog.ContactActionLog;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.contact.ContactController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ContactItemController extends AbstractController implements Initializable{

	ResourceBundle resourceBundle;

	@FXML
	Label alias;
	@FXML
	Label email;
	@FXML
	Pane avatarContainer;
	@FXML
	HBox contactRootItem;
	@Inject
	private ContactRepository contactRepository;

	ContactController parent;

	List<Consumer> selectionListeners = new LinkedList<>();

	@Inject
	private Contact contact;
	@Inject
	private ClientConfiguration clientConfiguration;
	@Inject
	private IdentityRepository identityRepository;
	@Inject
	private DropMessageRepository dropMessageRepository;

	private Indicator indicator = new Indicator();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		contactRootItem.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			for (Consumer listener : selectionListeners) {
				SelectionEvent selectionEvent = new SelectionEvent();
				selectionEvent.setContact(contact);
				selectionEvent.setController(this);
				listener.accept(selectionEvent);
			}
		});
        resourceBundle = resources;
		alias.setText(contact.getAlias());
		email.setText(contact.getEmail());
		updateAvatar();

		indicator.setVisible(false);
		final Actionlog actionlog = new ContactActionLog(contact, dropMessageRepository);

		actionlog.addObserver(message -> {
			refreshIndicator(actionlog);
		});
	}

	public Indicator getIndicator() {
		return indicator;
	}

	private void refreshIndicator(Actionlog actionlog) {
		Platform.runLater(() -> {
			int unseenMessageCount = actionlog.getUnseenMessageCount();
			indicator.setVisible(unseenMessageCount > 0);
			indicator.setText(String.valueOf(unseenMessageCount));
		});
	}

	public void addSelectionListener(Consumer<SelectionEvent> consumer) {
		selectionListeners.add(consumer);
	}

	public void select() {
		contactRootItem.getStyleClass().add("selected");
	}

	private void updateAvatar() {
		new AvatarView(e -> contact.getAlias()).getViewAsync(view -> {
			avatarContainer.getChildren().setAll(view);
			avatarContainer.getChildren().add(indicator);
		});
	}

	public void unselect() {
		contactRootItem.getStyleClass().remove("selected");
	}

	@FXML
	protected void handleDeleteContactsButtonAction() {
		Identity i = clientConfiguration.getSelectedIdentity();
		try {
			contactRepository.delete(contact, i);
		} catch (PersistenceException  e) {
			alert("Failed to delete Contact: " + contact.getAlias(), e);
		}
	}
}
