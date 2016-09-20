package de.qabel.desktop.ui.contact;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.repository.inmemory.InMemoryDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static de.qabel.desktop.ui.contact.ContactController.ContactsFilter.ALL;
import static de.qabel.desktop.ui.contact.ContactController.ContactsFilter.IGNORED;
import static de.qabel.desktop.ui.contact.ContactController.ContactsFilter.NEW;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class ContactControllerTest extends AbstractControllerTest {

    private static final String TEST_FOLDER = "tmp/test";


    ContactController controller;

    @After
    public void after() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_FOLDER));
    }

    @Test
    public void injectTest() {
        Locale.setDefault(new Locale("de", "DE"));
        ContactView view = new ContactView();
        Identity i = new Identity("test", null, new QblECKeyPair());
        clientConfiguration.selectIdentity(i);
        clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
        controller = (ContactController) view.getPresenter();
        assertNotNull(controller);
    }

    @Test
    public void changeIdentityObserverTest() throws Exception {
        getContact("One");
        Contact contact = getContact("Two");
        controller = getController();

        assertEquals(1, controller.contactsFromRepo.getContacts().size());
        assertEquals("Two", controller.contactsFromRepo.getByKeyIdentifier(contact.getKeyIdentifier()).getAlias());

    }

    @Test
    public void indicatorStartsInvisible() throws Exception {
        getContact("One");
        controller = getController();
        waitUntil(() -> controller.contactItems.size() > 0);
        assertFalse(controller.contactItems.get(0).getIndicator().isVisible());
    }

    @Test
    public void indicatorShowsUnUnreadMessages() throws Exception {
        Contact contact = getContact("One");
        controller = getController();
        waitUntil(() -> controller.contactItems.size() > 0);

        dropMessageRepository.addMessage(new DropMessage(contact, "a", "b"), contact, identity, false);
        waitUntil(() -> controller.contactItems.get(0).getIndicator().isVisible());
        assertEquals("1", controller.contactItems.get(0).getIndicator().getText());
    }

    @Test
    public void indicatorHidesWhenMessageIsRead() throws Exception {
        Contact contact = getContact("One");
        controller = getController();
        waitUntil(() -> controller.contactItems.size() > 0);

        dropMessageRepository.addMessage(new DropMessage(contact, "a", "b"), contact, identity, false);
        waitUntil(() -> controller.contactItems.get(0).getIndicator().isVisible());

        ((InMemoryDropMessageRepository)dropMessageRepository).lastMessage.setSeen(true);
        waitUntil(() -> !controller.contactItems.get(0).getIndicator().isVisible());
    }

    private Contact createContact(ContactController.ContactsFilter filter) throws Exception {
        Identity i = identityBuilderFactory.factory().withAlias("unknown").build();
        Contact c = new Contact("contact", i.getDropUrls(), i.getEcPublicKey());
        switch (filter) {
            case ALL:
                c.setStatus(Contact.ContactStatus.NORMAL);
                break;
            case NEW:
                c.setStatus(Contact.ContactStatus.UNKNOWN);
                break;
            case IGNORED:
                c.setStatus(Contact.ContactStatus.NORMAL);
                c.setIgnored(true);
                break;
        }
        contactRepository.save(c, identity);
        return c;
    }

    @Test
    public void filterContacts() throws Exception {
        Contact unknown1 = createContact(NEW);
        Contact unknown2 = createContact(NEW);
        Contact known = createContact(ALL);
        Contact ignored = createContact(IGNORED);
        List<Contact> contacts = Arrays.asList(unknown1, unknown2, known, ignored);
        controller = getController();
        assertThat(controller.filteredContacts(contacts, NEW), containsInAnyOrder(unknown1, unknown2));
        assertThat(controller.filteredContacts(contacts, ALL), containsInAnyOrder(known, unknown1, unknown2));
        assertThat(controller.filteredContacts(contacts, IGNORED), containsInAnyOrder(ignored));
    }

    private Contact getContact(String name) throws PersistenceException {
        Identity i = identityBuilderFactory.factory().withAlias(name).build();
        Contact c = new Contact(name, i.getDropUrls(), i.getEcPublicKey());
        contactRepository.save(c, i);
        clientConfiguration.selectIdentity(i);
        return c;
    }

    private ContactController getController() {
        ContactView view = new ContactView();
        view.getView();
        return (ContactController) view.getPresenter();
    }

}
