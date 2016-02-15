package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;


public class PersistenceDropMessageRepository extends AbstractCachedPersistenceRepository<Contact> implements DropMessageRepository{

	public PersistenceDropMessageRepository(Persistence<String> persistence) {
		super(persistence);
	}


	@Override
	public void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException {
		PersistenceDropMessage persistenceDropMessage = new PersistenceDropMessage(dropMessage, contact, send);
		persistence.updateOrPersistEntity(persistenceDropMessage);
		setChanged();
		notifyObservers();
	}

	@Override
	public List<PersistenceDropMessage> loadConversation(Contact contact) throws PersistenceException {
		List<PersistenceDropMessage> result = new LinkedList<>();
		List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);
		for (PersistenceDropMessage d : messages) {
			if (d.getContact() == contact) {
				result.add(d);
			}
		}

		return result;
	}

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
	}
}
