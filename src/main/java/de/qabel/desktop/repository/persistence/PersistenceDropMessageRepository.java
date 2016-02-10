package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;


public class PersistenceDropMessageRepository extends AbstractCachedPersistenceRepository<Contact> implements DropMessageRepository {

	public PersistenceDropMessageRepository(Persistence<String> persistence) {
		super(persistence);
	}


	@Override
	public void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException {
		PersistenceDropMessage persistenceDropMessage = new PersistenceDropMessage(dropMessage, from, to, send);
		persistence.updateOrPersistEntity(persistenceDropMessage);
		setChanged();
		notifyObservers();
	}

	@Override
	public List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
		List<PersistenceDropMessage> result = new LinkedList<>();
		List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);
		for (PersistenceDropMessage d : messages) {
			try {
				String contactKeyIdentifier = contact.getEcPublicKey().getReadableKeyIdentifier();
				String ownKeyIdentifier = identity.getEcPublicKey().getReadableKeyIdentifier();
				String senderIdentifier = d.getSender().getEcPublicKey().getReadableKeyIdentifier();
				String receiverKeyIdentifier = d.getReceiver().getEcPublicKey().getReadableKeyIdentifier();
				if ((senderIdentifier.equals(contactKeyIdentifier) && receiverKeyIdentifier.equals(ownKeyIdentifier) ||
						(senderIdentifier.equals(ownKeyIdentifier) && receiverKeyIdentifier.equals(contactKeyIdentifier)))) {
					result.add(d);
				}
			} catch (NullPointerException e){
				continue;
			}
		}
		return result;
	}

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
	}
}
