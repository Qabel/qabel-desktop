package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.Stub.StubDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

public class ActionlogTest extends AbstractControllerTest {
	private StubDropMessageRepository repo = new StubDropMessageRepository();
	private Actionlog log;
	private DropMessage msg;
	private Contact contact = new Contact("alias", null, new QblECKeyPair().getPub());
	private List<PersistenceDropMessage> notifications = new LinkedList<>();

	@Before
	public void setUp() throws Exception {
		super.setUp();
		log = new Actionlog(repo);
		msg = new DropMessage(contact, "payload", "type");
		log.addObserver(message -> notifications.add(message));
	}

	@Test
	public void testActionlogKnowsUnseenMessages() throws Exception {
		repo.addMessage(msg, contact, identity, false);

		assertEquals(1, log.getUnseenMessageCount());
		waitUntil(() -> notifications.size() == 1);
		assertEquals(repo.lastMessage, notifications.get(0));
	}

	@Test
	public void actionlogKnowsWhenMessageWasSeen() throws Exception {
		repo.addMessage(msg, contact, identity, false);
		waitUntil(() -> notifications.size() == 1);
		notifications.clear();
		repo.lastMessage.setSeen(true);

		assertEquals(0, log.getUnseenMessageCount());
		waitUntil(() -> notifications.size() == 1);
	}
}
