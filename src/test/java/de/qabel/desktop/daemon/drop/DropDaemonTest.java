package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.core.http.DropServerHttp;
import de.qabel.core.http.MainDropConnector;
import de.qabel.core.http.MockDropServer;
import de.qabel.core.repository.entities.ChatDropMessage;
import de.qabel.core.repository.entities.DropState;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.inmemory.InMemoryChatDropMessageRepository;
import de.qabel.core.service.ChatService;
import de.qabel.core.service.MainChatService;
import de.qabel.desktop.repository.inmemory.InMemoryHttpDropConnector;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.connector.DropConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DropDaemonTest extends AbstractControllerTest {
    private Contact c;
    private Thread daemon;
    private Identity identity;
    private Contact sender;
    private Identity senderIdentity;
    private DropDaemon dd;
    private ChatService chatService;
    private MainDropConnector dropConnector;
    private DropURL dropURL;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        identity = identityBuilderFactory.factory().withAlias("Tester").build();
        identityRepository.save(identity);
        clientConfiguration.selectIdentity(identity);
        c = getContact(identity);
        dropURL = identity.getDropUrls().iterator().next();
        senderIdentity = identityBuilderFactory.factory().withAlias("sender").build();
        sender = getContact(senderIdentity);
        contactRepository.save(sender, identity);
        dropConnector= new MainDropConnector(new MockDropServer());
        chatService = new MainChatService(dropConnector, identityRepository,
            contactRepository, chatDropMessageRepository, dropStateRepository);
        dd = new DropDaemon(chatService, dropMessageRepository, contactRepository);
    }

    @Test
    public void receiveMessagesTest() throws URISyntaxException, QblDropInvalidURL, QblNetworkInvalidResponseException, PersistenceException, EntityNotFoundException {
        ChatDropMessage.MessagePayload.TextMessage textMessage =
            new ChatDropMessage.MessagePayload.TextMessage("test_message");
        DropMessage dropMessage = new DropMessage(sender, textMessage.toString(),
            ChatDropMessage.MessageType.BOX_MESSAGE.getType());
        dropConnector.sendDropMessage(senderIdentity, c, dropMessage, dropURL);

        dd.receiveMessages();

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(sender, identity);
        assertEquals(1, lst.size());
    }
    /*

    @Test
    public void handlesErrorsTest() throws Exception {
        dd.setSleepTime(1);

        dropConnector.send(c, new DropMessage(sender, "test", "test"));
        dropConnector.throwException(new IllegalStateException("network error"));

        startDaemon();

        waitUntil(() -> dropConnector.getPolls() > 0);
        dropConnector.throwException(null);

        assertAsync(() -> dropMessageRepository.loadConversation(sender, identity), is(not(empty())));
    }

    @Test
    public void receivesMessagesForAllIdentities() throws Exception {
        Identity otherIdentity = identityBuilderFactory.factory().withAlias("tester2").build();
        Contact otherIdentitiesContact = getContact(otherIdentity);
        identityRepository.save(otherIdentity);
        contactRepository.save(sender, otherIdentity);

        DropMessage dropMessage = new DropMessage(sender, "Test", "test_message");
        dropConnector.send(otherIdentitiesContact, dropMessage);

        dd.receiveMessages();

        assertThat(dropMessageRepository.loadConversation(sender, otherIdentity), is(not(empty())));
    }
    */

    private Contact getContact(Identity otherIdentity) {
        return new Contact(otherIdentity.getAlias(), otherIdentity.getDropUrls(), otherIdentity.getEcPublicKey());
    }

    private void startDaemon() {
        daemon = new Thread(dd);
        daemon.setDaemon(true);
        daemon.start();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        if (daemon != null && daemon.isAlive()) {
            daemon.interrupt();
        }
        super.tearDown();
    }
}
