package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.inmemory.InMemoryDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ContactActionLogTest extends AbstractControllerTest {
    private InMemoryDropMessageRepository repo = new InMemoryDropMessageRepository();
    private Actionlog log;
    private DropMessage msg;
    private DropMessage otherMsg;
    private Contact contact = new Contact("alias", null, new QblECKeyPair().getPub());
    private Contact other = new Contact("alias2", null, new QblECKeyPair().getPub());
    private List<PersistenceDropMessage> notifications = new LinkedList<>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        log = new ContactActionLog(identity, contact, repo);
        msg = new DropMessage(contact, "payload", "type");
        otherMsg = new DropMessage(contact, "payload", "type");
        log.addObserver(message -> notifications.add(message));
    }

    @Test
    public void forwardsMessagesFromContact() throws Exception {
        repo.addMessage(msg, contact, identity, false);

        assertEquals(1, log.getUnseenMessageCount());
        waitUntil(() -> notifications.size() == 1);
    }

    @Test
    public void ignoresOtherContacts() throws Exception {
        repo.addMessage(msg, other, identity, false);

        assertEquals(0, log.getUnseenMessageCount());
        waitUntil(() -> notifications.size() == 0);
    }

    @Test
    public void forwardsMessagesToContact() throws Exception {
        repo.addMessage(msg, identity, contact, true);

        assertEquals(0, log.getUnseenMessageCount());
        waitUntil(() -> notifications.size() == 1);
    }

    @Test
    public void loadsExistingUnseenMessages() throws Exception {
        repo.addMessage(msg, identity, contact, false);

        log = new ContactActionLog(identity, contact, repo);
        assertThat(log.getUnseenMessageCount(), is(1));
        for (PersistenceDropMessage message : repo.loadConversation(contact, identity)) {
            message.setSeen(true);
        }
        assertAsync(log::getUnseenMessageCount, is(0));
    }

    @Test
    public void loadsOnlyUnseenMessages() throws Exception {
        repo.addMessage(otherMsg, identity, contact, false);
        List<PersistenceDropMessage> messages = repo.loadConversation(contact, identity);
        for (PersistenceDropMessage message : messages) {
            message.setSeen(true);
        }

        repo.addMessage(msg, identity, contact, false);
        log = new ContactActionLog(identity, contact, repo);
        assertThat(log.getUnseenMessageCount(), is(1));
    }
}
