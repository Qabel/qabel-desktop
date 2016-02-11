package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.*;


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

		String contactKeyIdentifier = contact.getEcPublicKey().getReadableKeyIdentifier();
		String ownKeyIdentifier = identity.getEcPublicKey().getReadableKeyIdentifier();

		for (PersistenceDropMessage d : messages) {

			if (belongsToConversation(d, contactKeyIdentifier, ownKeyIdentifier)) {
				result.add(d);
			}
		}
		return result;
	}


	@Override
	public List<PersistenceDropMessage> loadNewMessagesFromConversation(List<PersistenceDropMessage> dropMessages, Contact c, Identity identity) {
		List<PersistenceDropMessage> result = new LinkedList<>();

		Map<String, PersistenceDropMessage> map = new HashMap<>();

		for (PersistenceDropMessage m : dropMessages) {
			map.put(m.getPersistenceID(), m);
		}

		String contactKeyIdentifier = c.getEcPublicKey().getReadableKeyIdentifier();
		String ownKeyIdentifier = identity.getEcPublicKey().getReadableKeyIdentifier();

		List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);
		for (PersistenceDropMessage m : messages) {
			if (!map.containsKey(m.getPersistenceID())) {
				if (belongsToConversation(m, contactKeyIdentifier, ownKeyIdentifier)) {
					result.add(m);
				}
			}
		}
		return result;
	}


	private boolean belongsToConversation(PersistenceDropMessage dropMessage, String contactKeyIdentifier, String ownKeyIdentifier) {

		String senderIdentifier = dropMessage.getSender().getEcPublicKey().getReadableKeyIdentifier();
		String receiverKeyIdentifier = dropMessage.getReceiver().getEcPublicKey().getReadableKeyIdentifier();

		return (senderIdentifier.equals(contactKeyIdentifier) && receiverKeyIdentifier.equals(ownKeyIdentifier)
				|| (senderIdentifier.equals(ownKeyIdentifier) && receiverKeyIdentifier.equals(contactKeyIdentifier)));
	}

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
	}
}
