package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

public class StubDropMessageRepository implements DropMessageRepository {

	private HashMap<String,List<PersistenceDropMessage>> messagesMap = new HashMap<>();


	@Override
	public void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException {
		PersistenceDropMessage pdm = new PersistenceDropMessage(dropMessage,contact,send);

		List<PersistenceDropMessage> lst = messagesMap.get(contact.getKeyIdentifier());
		if(lst == null){
			lst = new LinkedList<>();
			lst.add(pdm);
		}
		messagesMap.put(contact.getKeyIdentifier(), lst);
	}

	@Override
	public List<PersistenceDropMessage> loadConversation(Contact contact) throws PersistenceException {
		List<PersistenceDropMessage> lst = messagesMap.get(contact.getKeyIdentifier());
		if(lst == null){
			return new LinkedList<>();
		}
		return lst;
	}

	@Override
	public void addObserver(Observer o) {

	}
}
