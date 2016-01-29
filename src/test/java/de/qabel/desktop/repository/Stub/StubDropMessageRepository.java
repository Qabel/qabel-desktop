package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersitsDropMessage;

import java.util.LinkedList;
import java.util.List;

public class StubDropMessageRepository implements DropMessageRepository {
	private List<PersitsDropMessage> messages = new LinkedList<>();



	@Override
	public void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException {
		PersitsDropMessage pdm = new PersitsDropMessage(dropMessage,contact,send);
		messages.add(pdm);
	}

	@Override
	public List<PersitsDropMessage> loadConversation(Contact contact) throws PersistenceException {
		return messages;
	}
}
