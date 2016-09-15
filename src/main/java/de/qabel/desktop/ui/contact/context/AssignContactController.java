package de.qabel.desktop.ui.contact.context;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class AssignContactController extends AbstractController implements Initializable {
    @Inject
    private Contact contact;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private ContactRepository contactRepository;

    @FXML
    GridPane container;

    private List<Identity> shownIdentities = new LinkedList<>();
    private Set<Identity> assignedIdentities = new HashSet<>();
    private Map<Identity, ToggleButton> buttonByIdentity = new HashMap<>();
    private Map<ToggleButton, Identity> identityByButton = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        container.getChildren().clear();

        try {
            try {
                contactRepository
                    .findContactWithIdentities(contact.getKeyIdentifier())
                    .getIdentities()
                    .stream()
                    .forEach(assignedIdentities::add);
            } catch (EntityNotFoundException ignored) {
                // gets fired if contactRepository is completely unassigned
            }

            identityRepository.findAll().getIdentities().stream()
                .sorted((o1, o2) -> o1.getAlias().compareTo(o2.getAlias()))
                .forEach(this::addIdentity);
        } catch (PersistenceException e) {
            alert(e);
        }
    }

    private void addIdentity(Identity identity) {
        shownIdentities.add(identity);
        int row = shownIdentities.indexOf(identity);
        Label label = new Label(identity.getAlias());
        container.add(label, 0, row);

        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setId("assign-" + identity.getId());
        toggleButton.setSelected(isNotUnknown() && assignedIdentities.contains(identity));
        toggleButton.setOnAction(this::toggle);
        toggleButton.getStyleClass().add("switch");
        buttonByIdentity.put(identity, toggleButton);
        identityByButton.put(toggleButton, identity);
        container.add(toggleButton, 1, row);
    }

    private boolean isNotUnknown() {
        return contact.getStatus() != Contact.ContactStatus.UNKNOWN;
    }

    ToggleButton getButtonForIdentity(Identity identity) {
        return buttonByIdentity.get(identity);
    }

    private void toggle(ActionEvent event) {
        tryOrAlert(() -> {
            Object source = event.getSource();
            ToggleButton button = (ToggleButton) source;
            Identity identity = identityByButton.get(button);
            if (button.isSelected()) {
                assign(identity);
            } else {
                unassign(identity);
            }
        });
    }

    private void unassign(Identity identity) throws PersistenceException {
        contactRepository.delete(contact, identity);
    }

    private void assign(Identity identity) throws PersistenceException {
        if (contact.getStatus() == Contact.ContactStatus.UNKNOWN) {
            unassignAll();
            contact.setStatus(Contact.ContactStatus.NORMAL);
        }
        contactRepository.save(contact, identity);
    }

    private void unassignAll() throws PersistenceException {
        try {
            for (Identity identity : contactRepository.findContactWithIdentities(contact.getKeyIdentifier()).getIdentities()) {
                unassign(identity);
            }
        } catch (EntityNotFoundException ignored) {}
    }
}
