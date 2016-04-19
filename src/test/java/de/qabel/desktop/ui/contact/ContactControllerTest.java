package de.qabel.desktop.ui.contact;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

public class ContactControllerTest extends AbstractControllerTest {

    private static final String TEST_FOLDER = "tmp/test";
    private static final String TEST_JSON = "/TestContacts.json";
    private static final String TEST_ALIAS = "TestAlias";

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
    public void exportContactsTest() throws URISyntaxException, EntityNotFoundExcepion, IOException, QblDropInvalidURL, PersistenceException, JSONException {
        Identity i = identityBuilderFactory.factory().withAlias(TEST_ALIAS).build();
        clientConfiguration.selectIdentity(i);

        Contact c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
        c.setPhone("000");
        c.setEmail("abc");
        contactRepository.save(c, i);

        controller = getController();
        File testDir = new File(TEST_FOLDER);
        testDir.mkdirs();
        File file = new File(testDir + "/contacts.json");
        controller.exportContacts(file);
        Contacts contacts = contactRepository.find(i);
        assertEquals(1, contacts.getContacts().size());

        contacts.remove(c);
        assertEquals(0, contacts.getContacts().size());

        controller.importContacts(file);

        contacts = contactRepository.find(i);
        assertEquals(1, contacts.getContacts().size());

        List<Contact> l = new LinkedList<>(contacts.getContacts());

        Contact contact0 = l.get(0);

        assertEquals(contact0.getAlias(), c.getAlias());
        assertEquals(contact0.getEmail(), c.getEmail());
        assertEquals(contact0.getCreated(), c.getCreated(), 100000);
        assertEquals(contact0.getDeleted(), c.getDeleted());
        assertEquals(contact0.getUpdated(), c.getUpdated());
        assertEquals(contact0.getDropUrls(), c.getDropUrls());
        assertEquals(contact0.getEcPublicKey(), c.getEcPublicKey());
    }

    @Test
    public void importContactsTest() throws URISyntaxException, PersistenceException, IOException, QblDropInvalidURL, EntityNotFoundExcepion, JSONException {
        Identity i = identityBuilderFactory.factory().withAlias(TEST_ALIAS).build();
        clientConfiguration.selectIdentity(i);
        File f = new File(System.class.getResource(TEST_JSON).toURI());
        controller = getController();
        controller.importContacts(f);

        Contacts contacts = contactRepository.find(i);
        assertEquals(1, contacts.getContacts().size());


        Contact c = contacts.getByKeyIdentifier("0c403e258baf03d19955d5b5fea1fecabc82ac65f304962af8e47c2135a30a36");
        assertEquals(c.getAlias(),"TestAlias");
        assertEquals(c.getEmail(), "abc");
        assertEquals(c.getPhone(), "000");
        assertEquals(c.getDropUrls().size(), 1);
        assertNotNull(c.getEcPublicKey());
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

        dropMessageRepository.lastMessage.setSeen(true);
        waitUntil(() -> !controller.contactItems.get(0).getIndicator().isVisible());
    }

    @Test
    public void importValidContactAfterFailedImport() throws Exception {
        Contact contact1 = new Contact("one", new HashSet<>(), new QblECPublicKey("one".getBytes()));
        Contact contact2 = new Contact("two", new HashSet<>(), new QblECPublicKey("two".getBytes()));

        Path c1 = Files.createTempFile("testcontact", "one");
        Path c2 = Files.createTempFile("testcontact", "two");
        Files.write(c1, ContactExportImport.exportContact(contact1).getBytes());
        Files.write(c2, ContactExportImport.exportContact(contact2).getBytes());

        controller = getController();
        controller.importContacts(c1.toFile());
        try {
            controller.importContacts(c1.toFile());
            fail("duplicate import failed, cannot continue test");
        } catch (Exception ignored) {}

        controller.importContacts(c2.toFile());
        assertEquals(2, contactRepository.find(identity).getContacts().size());
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
