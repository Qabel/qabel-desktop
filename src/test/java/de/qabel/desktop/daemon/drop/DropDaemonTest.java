package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.inmemory.InMemoryHttpDropConnector;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.connector.DropConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

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

        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("Tester").build();
        identityRepository.save(identity);
        clientConfiguration.selectIdentity(identity);
        c = new Contact(identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
        sender = new Contact("sender", new HashSet<>(), new QblECPublicKey("sender".getBytes()));
        contactRepository.save(sender, identity);
        dd = new DropDaemon(clientConfiguration, dropConnector, contactRepository, dropMessageRepository);
    }

    @Test
    public void receiveMessagesTest() throws URISyntaxException, QblDropInvalidURL, QblNetworkInvalidResponseException, PersistenceException, EntityNotFoundException {
        DropMessage dropMessage = new DropMessage(sender, "Test", "test_message");
        dropConnector.send(c, dropMessage);

        dd.receiveMessages();

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, identity);
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

        assertAsync(() -> dropMessageRepository.loadConversation(c, identity), is(not(empty())));
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
