package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.inmemory.InMemoryHttpDropConnector;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.connector.DropConnector;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DropDaemonTest extends AbstractControllerTest {

	DropConnector dropConnector = new InMemoryHttpDropConnector();;
	Contact c;
	String fakeURL = "http://localhost:12345/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl";

	@Test
	public void receiveMessagesTest() throws URISyntaxException, QblDropInvalidURL, QblNetworkInvalidResponseException, PersistenceException, EntityNotFoundExcepion {
		Collection<DropURL> collection = new ArrayList<>();
		DropURL dropURL = new DropURL(fakeURL);
		collection.add(dropURL);
		Identity identity = new Identity("TestAlias", collection, new QblECKeyPair());
		clientConfiguration.selectIdentity(identity);
		c = new Contact(identity.getAlias(), collection, identity.getEcPublicKey());
		DropMessage dropMessage = new DropMessage(identity, "Test", "test_message");

		dropConnector.send(c, dropMessage);

		DropDaemon dd = new DropDaemon(clientConfiguration, dropConnector, contactRepository, dropMessageRepository);
		dd.receiveMessages();

		List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, identity);
		assertEquals(1, lst.size());

	}
}
