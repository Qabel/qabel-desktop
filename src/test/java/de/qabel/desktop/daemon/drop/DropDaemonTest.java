package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.inmemory.InMemoryHttpDropConnector;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
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
    private InMemoryHttpDropConnector dropConnector = new InMemoryHttpDropConnector();
    private Contact c;
    private Thread daemon;
    private Identity identity;
    private Contact sender;
    private DropDaemon dd;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        identity = identityBuilderFactory.factory().withAlias("Tester").build();
        identityRepository.save(identity);
        clientConfiguration.selectIdentity(identity);
        c = getContact(identity);
        sender = new Contact("sender", new HashSet<>(), new QblECPublicKey("sender".getBytes()));
        contactRepository.save(sender, identity);
        dd = new DropDaemon(
            clientConfiguration,
            dropConnector,
            contactRepository,
            dropMessageRepository,
            identityRepository
        );
    }

    @Test
    public void receiveMessagesTest() throws URISyntaxException, QblDropInvalidURL, QblNetworkInvalidResponseException, PersistenceException, EntityNotFoundException {
        DropMessage dropMessage = new DropMessage(sender, "Test", "test_message");
        dropConnector.send(c, dropMessage);

        dd.receiveMessages();

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(sender, identity);
        assertEquals(1, lst.size());
    }

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
