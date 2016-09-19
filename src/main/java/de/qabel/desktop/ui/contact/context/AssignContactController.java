package de.qabel.desktop.ui.contact.context;

import com.jfoenix.controls.JFXToggleButton;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.contacts.ContactData;
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
    private ToggleButton ignoreButton;

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
        addIgnoreButton(resources);
    }

    private void addIgnoreButton(ResourceBundle resources) {
        int row = shownIdentities.size();
        Label label = new Label(resources.getString("ignoreContact"));
        container.add(label, 0, row);
        JFXToggleButton toggleButton = new JFXToggleButton();
        toggleButton.setId("ignore-");
        toggleButton.setSelected(contact.isIgnored());
        toggleButton.setOnAction(this::ignore);
        container.add(toggleButton, 1, row);
    }

    private void ignore(ActionEvent actionEvent) {
        contact.setIgnored(ignoreButton.isSelected());
        tryOrAlert(() -> {
            ContactData withIdentities = contactRepository.findContactWithIdentities(contact.getKeyIdentifier());
            contactRepository.update(contact, withIdentities.getIdentities());
        });
    }

    private void addIdentity(Identity identity) {
        shownIdentities.add(identity);
        int row = shownIdentities.indexOf(identity);
        Label label = new Label(identity.getAlias());
        container.add(label, 0, row);
        JFXToggleButton toggleButton = new JFXToggleButton();
        toggleButton.setId("assign-" + identity.getId());
        toggleButton.setSelected(assignedIdentities.contains(identity));
        toggleButton.setOnAction(this::toggle);

        buttonByIdentity.put(identity, toggleButton);
        identityByButton.put(toggleButton, identity);
        container.add(toggleButton, 1, row);
    }

    ToggleButton getButtonForIdentity(Identity identity) {
        return buttonByIdentity.get(identity);
    }

    ToggleButton getIgnoreButton() {
        return ignoreButton;
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
            contact.setStatus(Contact.ContactStatus.NORMAL);
        }
        contactRepository.save(contact, identity);
    }

}
