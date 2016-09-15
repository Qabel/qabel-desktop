package de.qabel.desktop.ui.contact.context;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class AssignContactControllerTest extends AbstractControllerTest {
    private static final int ELEMENTS_PER_IDENTITY = 2;
    private AssignContactController controller;
    private Contact contact;
    private Identity assignedIdentity;
    private Identity unassignedIdentity;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        for (Identity identity : identityRepository.findAll().getIdentities()) {
            identityRepository.delete(identity);
        }

        contact = identityBuilderFactory.factory().withAlias("contact1").build().toContact();
        assignedIdentity = identityBuilderFactory.factory().withAlias("identity1").build();
        unassignedIdentity = identityBuilderFactory.factory().withAlias("identity2").build();

        identityRepository.save(assignedIdentity);
        identityRepository.save(unassignedIdentity);
        contactRepository.save(contact, assignedIdentity);
    }

    @Test
    public void loadsIdentities() throws Exception {
        initController();

        ObservableList<Node> children = controller.container.getChildren();
        assertThat(children, hasSize(2 * ELEMENTS_PER_IDENTITY));

        assertLabelWithText(children.get(0), "identity1");
        assertSelectedToggle(children.get(1));

        assertLabelWithText(children.get(ELEMENTS_PER_IDENTITY), "identity2");
        assertUnselectedToggle(children.get(ELEMENTS_PER_IDENTITY + 1));
    }

    private void assertUnselectedToggle(Node node) {
        assertSelectedToggle(node, false);
    }

    private void assertSelectedToggle(Node node) {
        assertSelectedToggle(node, true);
    }

    private void assertSelectedToggle(Node node, boolean selected) {
        assertThat(node, instanceOf(ToggleButton.class));
        ToggleButton button1 = (ToggleButton) node;
        assertThat(button1.isSelected(), is(selected));
    }

    private void assertLabelWithText(Node node, String text) {
        assertThat(node, instanceOf(Label.class));
        assertThat(((Label)node).getText(), equalTo(text));
    }

    private void initController() {
        AssignContactView view = new AssignContactView(contact);
        view.getView();
        controller = view.getPresenter();
    }

    @Test
    public void addsAssignment() throws Exception {
        initController();
        ToggleButton button = controller.getButtonForIdentity(unassignedIdentity);
        button.fire();

        assertThat(getAssignedIdentities(), hasItem(unassignedIdentity));
        assertThat(button.isSelected(), is(true));
    }

    @Test
    public void unacceptedContactsShowUnselected() throws Exception {
        contact.setStatus(Contact.ContactStatus.UNKNOWN);
        initController();
        ToggleButton assignedButton = controller.getButtonForIdentity(assignedIdentity);
        assertThat(assignedButton.isSelected(), is(false));
    }

    @Test
    public void unknownContactGetsAssignedToBothIdentities() throws Exception {
        contact.setStatus(Contact.ContactStatus.UNKNOWN);
        initController();
        controller.getButtonForIdentity(unassignedIdentity).fire();

        assertThat(getAssignedIdentities(), contains(unassignedIdentity));
        assertThat(getAssignedIdentities(), contains(assignedIdentity));
    }

    @Test
    public void acceptsContact() throws Exception {
        contact.setStatus(Contact.ContactStatus.UNKNOWN);
        contactRepository.delete(contact, assignedIdentity);
        initController();

        controller.getButtonForIdentity(assignedIdentity).fire();

        assertThat(contact.getStatus(), equalTo(Contact.ContactStatus.NORMAL));
    }

    @Test
    public void removesAssignment() throws Exception {
        initController();
        ToggleButton button = controller.getButtonForIdentity(assignedIdentity);
        button.fire();

        assertThat(getAssignedIdentities(), not(hasItem(assignedIdentity)));
        assertThat(button.isSelected(), is(false));
    }

    private List<Identity> getAssignedIdentities() throws PersistenceException {
        try {
            return contactRepository.findContactWithIdentities(contact.getKeyIdentifier())
                .getIdentities();
        } catch (EntityNotFoundException e) {
            return Collections.emptyList();
        }
    }
}
