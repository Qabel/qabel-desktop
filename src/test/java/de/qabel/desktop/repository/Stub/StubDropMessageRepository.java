package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

public class StubDropMessageRepository implements DropMessageRepository {
	private List<PersistenceDropMessage> messages = new LinkedList<>();



	@Override
	public void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException {
		PersistenceDropMessage pdm = new PersistenceDropMessage(dropMessage,contact,send);
		messages.add(pdm);
	}

	@Override
	public List<PersistenceDropMessage> loadConversation(Contact contact) throws PersistenceException {
		return messages;
	}

	@Override
	public void addObserver(Observer o) {

	}
}
