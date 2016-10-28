package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.*;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SqliteDropMessageRepositoryTest extends AbstractSqliteRepositoryTest<DropMessageRepository> {
    private IdentityRepository identityRepository;
    private ContactRepository contactRepository;
    private Identity identity;
    private Contact contact = new Contact("contact", new HashSet<>(), new QblECPublicKey("key".getBytes()));
    private DropMessage drop;
    private PersistenceDropMessage message;

    @Override
    protected DropMessageRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        identityRepository = new SqliteIdentityRepository(
            clientDatabase, em,
            new SqlitePrefixRepository(clientDatabase, em),
            new SqliteDropUrlRepository(clientDatabase, new DropURLHydrator())
        );
        contactRepository = new SqliteContactRepository(
            clientDatabase,
            em,
            new SqliteDropUrlRepository(clientDatabase, new DropURLHydrator()),
            identityRepository
        );

        return new SqliteDropMessageRepository(clientDatabase, em);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("identity").build();
        identityRepository.save(identity);
        contactRepository.save(contact, identity);
        drop = new DropMessage(contact, "payload", "type");
        message = new PersistenceDropMessage(drop, contact, identity, false, true);
    }

    @Test
    public void knowsAddedMessages() throws Exception {
        repo.save(message);
        List<PersistenceDropMessage> conversation = loadConversation();
        assertEquals(1, conversation.size());
        PersistenceDropMessage loaded = conversation.get(0);
        assertSame(message, loaded);
    }

    @Test
    public void knowsAddedMessagesFromLegacyMethod() throws Exception {
        final List<PersistenceDropMessage> updates = new LinkedList<>();
        repo.addObserver((o, arg) -> updates.add((PersistenceDropMessage) arg));

        repo.addMessage(message.getDropMessage(), message.getSender(), message.getReceiver(), message.isSent());
        List<PersistenceDropMessage> conversation = loadConversation();
        assertEquals(1, conversation.size());
        PersistenceDropMessage loaded = conversation.get(0);

        assertEquals("type", loaded.getDropMessage().getDropPayloadType());
        assertEquals("payload", loaded.getDropMessage().getDropPayload());
        assertSame(identity, loaded.getReceiver());
        assertSame(contact, loaded.getSender());
        assertEquals(false, loaded.isSent());

        assertEquals("observers have not been notified", 1, updates.size());
        assertSame(loaded, updates.get(0));
    }

    @Test
    public void loadsUncachedMessages() throws Exception {
        repo.save(message);
        em.clear();
        em.put(Identity.class, identity);
        em.put(Contact.class, contact);

        List<PersistenceDropMessage> conversation = loadConversation();
        assertEquals(1, conversation.size());
        PersistenceDropMessage loaded = conversation.get(0);

        assertEquals("type", loaded.getDropMessage().getDropPayloadType());
        assertEquals("payload", loaded.getDropMessage().getDropPayload());
        assertSame(identity, loaded.getReceiver());
        assertSame(contact, loaded.getSender());
        assertEquals(false, loaded.isSent());
        assertEquals(true, loaded.isSeen());
        assertEquals(contact.getKeyIdentifier(), loaded.getDropMessage().getSenderKeyId());
    }

    private List<PersistenceDropMessage> loadConversation() throws PersistenceException {
        return repo.loadConversation(contact, identity);
    }

    @Test
    public void loadsUncachedMessageWithUncachedRelations() throws Exception {
        repo.save(message);
        em.clear();

        List<PersistenceDropMessage> conversation = loadConversation();
        assertEquals(1, conversation.size());
        PersistenceDropMessage loaded = conversation.get(0);

        assertEquals(identity.getKeyIdentifier(), loaded.getReceiver().getKeyIdentifier());
        assertEquals(contact.getKeyIdentifier(), loaded.getSender().getKeyIdentifier());
    }

    @Test
    public void updatesExistingMessages() throws Exception {
        repo.save(message);

        message.setSeen(false);
        repo.save(message);
        em.clear();

        List<PersistenceDropMessage> conversation = loadConversation();
        assertEquals(1, conversation.size());
        PersistenceDropMessage loaded = conversation.get(0);
        assertFalse(loaded.isSeen());

        assertEquals("type", loaded.getDropMessage().getDropPayloadType());
        assertEquals("payload", loaded.getDropMessage().getDropPayload());
        assertEquals(identity.getKeyIdentifier(), loaded.getReceiver().getKeyIdentifier());
        assertEquals(contact.getKeyIdentifier(), loaded.getSender().getKeyIdentifier());
        assertEquals(false, loaded.isSent());
    }

    @Test
    public void hasLegacyLoadMethod() throws Exception {
        List<PersistenceDropMessage> conversation = repo.loadNewMessagesFromConversation(
            new LinkedList<>(),
            contact,
            identity
        );
        assertTrue(conversation.isEmpty());

        repo.save(message);


        List<PersistenceDropMessage> newMessages = repo.loadNewMessagesFromConversation(
            conversation,
            contact,
            identity
        );
        assertEquals(1, newMessages.size());
        assertSame(message, newMessages.get(0));

        newMessages = repo.loadNewMessagesFromConversation(newMessages, contact, identity);
        assertTrue(newMessages.isEmpty());
    }
}
