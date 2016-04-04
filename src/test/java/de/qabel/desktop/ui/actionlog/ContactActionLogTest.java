package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.Stub.StubDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

public class ContactActionLogTest extends AbstractControllerTest {
	private StubDropMessageRepository repo = new StubDropMessageRepository();
	private Actionlog log;
	private DropMessage msg;
	private Contact contact = new Contact("alias", null, new QblECKeyPair().getPub());
	private Contact other = new Contact("alias2", null, new QblECKeyPair().getPub());
	private List<PersistenceDropMessage> notifications = new LinkedList<>();

	@Override
    @Before
	public void setUp() throws Exception {
		super.setUp();
		log = new ContactActionLog(contact, repo);
		msg = new DropMessage(contact, "payload", "type");
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
		repo.addMessage(msg, contact, identity, true);

		assertEquals(0, log.getUnseenMessageCount());
		waitUntil(() -> notifications.size() == 1);
	}
}
