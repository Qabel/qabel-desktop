package de.qabel.desktop.daemon.drop;


import com.google.common.io.Files;
import de.qabel.chat.repository.inmemory.InMemoryChatShareRepository;
import de.qabel.chat.service.MainSharingService;
import de.qabel.chat.service.SharingService;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropMessageMetadata;
import de.qabel.core.drop.DropURL;
import de.qabel.core.http.DropServerHttp;
import de.qabel.core.http.MainDropConnector;
import de.qabel.core.http.MockDropServer;
import de.qabel.chat.repository.entities.ChatDropMessage;
import de.qabel.core.repository.entities.DropState;
import de.qabel.chat.service.ChatService;
import de.qabel.chat.service.MainChatService;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
    private MainDropConnector dropConnector;
    private MockCoreDropConnector mockCoreDropConnector;
    private SharingService sharingService;
    private ChatService chatService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        identity = identityBuilderFactory.factory().withAlias("Tester").build();
        identityRepository.save(identity);
        clientConfiguration.selectIdentity(identity);
        c = getContact(identity);
        senderIdentity = identityBuilderFactory.factory().withAlias("sender").build();
        sender = getContact(senderIdentity);
        contactRepository.save(sender, identity);
        dropConnector= new MainDropConnector(new MockDropServer());
        mockCoreDropConnector = new MockCoreDropConnector();
        sharingService = new MainSharingService(new InMemoryChatShareRepository(), contactRepository,
            Files.createTempDir(), new CryptoUtils());
        chatService = new MainChatService(mockCoreDropConnector, identityRepository,
            contactRepository, chatDropMessageRepository, dropStateRepository, sharingService);
        dd = new DropDaemon(chatService, dropMessageRepository, contactRepository, identityRepository);
    }

    private void send(Contact contact, Identity sender) {
        ChatDropMessage.MessagePayload.TextMessage textMessage =
            new ChatDropMessage.MessagePayload.TextMessage("test_message");
        DropMessage dropMessage = new DropMessage(sender, textMessage.toString(),
            ChatDropMessage.MessageType.BOX_MESSAGE.getType());
        DropMessageMetadata metadata = new DropMessageMetadata(sender);
        dropMessage.setDropMessageMetadata(metadata);
        dropConnector.sendDropMessage(sender, contact, dropMessage, contact.getDropUrls().iterator().next());
    }

    @Test
    public void receiveMessagesTest() throws Exception {
        send(c, senderIdentity);

        dd.receiveMessages();

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(sender, identity);
        assertEquals(1, lst.size());
    }

    @Test
    public void helloProtocol() throws Exception {
        Identity otherIdentity = identityBuilderFactory.factory().withAlias("tester2").build();
        Contact otherIdentitiesContact = getContact(otherIdentity);
        send(c, otherIdentity);

        dd.receiveMessages();

        Contact other = contactRepository.findByKeyId(identity, otherIdentitiesContact.getKeyIdentifier());
        assertThat(otherIdentitiesContact.getAlias(), equalTo(other.getAlias()));
        assertThat(other.getStatus(), equalTo(Contact.ContactStatus.UNKNOWN));
    }

    @Test
    public void handlesErrorsTest() throws Exception {
        dd.setSleepTime(1);
        send(c, senderIdentity);

        mockCoreDropConnector.e = new IllegalStateException("network error");

        startDaemon();

        waitUntil(() -> mockCoreDropConnector.polls > 0);
        mockCoreDropConnector.e = null;

        assertAsync(() -> dropMessageRepository.loadConversation(sender, identity), is(not(empty())));
    }

    @Test
    public void receivesMessagesForAllIdentities() throws Exception {
        Identity otherIdentity = identityBuilderFactory.factory().withAlias("tester2").build();
        Contact otherIdentitiesContact = getContact(otherIdentity);
        identityRepository.save(otherIdentity);
        contactRepository.save(sender, otherIdentity);

        send(otherIdentitiesContact, senderIdentity);

        dd.receiveMessages();

        assertThat(dropMessageRepository.loadConversation(sender, otherIdentity), is(not(empty())));
    }

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

    private class MockCoreDropConnector implements de.qabel.core.http.DropConnector {
        public RuntimeException e = null;
        int polls;

        @Override
        public void sendDropMessage(Identity identity, Contact contact, DropMessage dropMessage, DropURL dropURL) { }

        @NotNull
        @Override
        public DropServerHttp.DropServerResponse<DropMessage> receiveDropMessages(Identity identity, DropURL dropURL, DropState dropState) {
            polls++;
            if (e != null) {
                throw e;
            }
            return dropConnector.receiveDropMessages(identity, dropURL, dropState);
        }
    }
}
