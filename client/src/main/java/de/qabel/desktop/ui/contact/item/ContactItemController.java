package de.qabel.desktop.ui.contact.item;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.Indicator;
import de.qabel.desktop.ui.accounting.avatar.ContactAvatarView;
import de.qabel.desktop.ui.accounting.item.SelectionEvent;
import de.qabel.desktop.ui.actionlog.ContactActionLog;
import de.qabel.desktop.ui.actionlog.FxActionlog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ContactItemController extends AbstractController implements Initializable {

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

    List<Consumer> selectionListeners = new LinkedList<>();
    List<Consumer> contextListeners = new LinkedList<>();

    @Inject
    Contact contact;
    @Inject
    private ClientConfig clientConfiguration;
    @Inject
    private IdentityRepository identityRepository;
    @Inject
    private DropMessageRepository dropMessageRepository;

    private Indicator indicator = new Indicator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contactRootItem.getStyleClass().add("contact-" + contact.getId());
        contactRootItem.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            List<Consumer> listeners = selectionListeners;
            if (event.getButton() == MouseButton.SECONDARY) {
                listeners = contextListeners;
            }
            listeners.forEach( listener -> {
                SelectionEvent selectionEvent = new SelectionEvent(event.getScreenX(), event.getScreenY());
                selectionEvent.setContact(contact);
                selectionEvent.setController(this);
                listener.accept(selectionEvent);
                event.consume();
            });
        });
        resourceBundle = resources;
        alias.setText(contact.getAlias());
        email.setText(contact.getEmail());
        updateAvatar();

        indicator.setVisible(false);
        final FxActionlog actionlog = new FxActionlog(new ContactActionLog(
            clientConfiguration.getSelectedIdentity(),
            contact,
            dropMessageRepository
        ));

        indicator.visibleProperty().bind(indicator.textProperty().isNotEqualTo("0"));
        indicator.textProperty().bind(actionlog.unseenMessageCountAsStringProperty());
    }

    public HBox getContactRootItem() {
        return contactRootItem;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public void addSelectionListener(Consumer<SelectionEvent> consumer) {
        selectionListeners.add(consumer);
    }

    public void addContextListener(Consumer<SelectionEvent> consumer) {
        contextListeners.add(consumer);
    }

    public void select() {
        contactRootItem.getStyleClass().add("selected");
    }

    private void updateAvatar() {
        final Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("alias", contact.getAlias());
        injectionContext.put("unknown", contact.getStatus() == Contact.ContactStatus.UNKNOWN);
        new ContactAvatarView(injectionContext::get).getViewAsync(view -> {
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
        } catch (PersistenceException e) {
            alert("Failed to delete Contact: " + contact.getAlias(), e);
        }
    }
}
