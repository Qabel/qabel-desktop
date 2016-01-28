package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersitsDropMessage;

import java.util.LinkedList;
import java.util.List;


public class PersistenceDropMessageRepository extends AbstractCachedPersistenceRepository<Contact> implements DropMessageRepository {

	public PersistenceDropMessageRepository(Persistence<String> persistence) {
		super(persistence);
	}


	@Override
	public void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException {
		PersitsDropMessage persitsDropMessage = new PersitsDropMessage(dropMessage, contact, send);
		persistence.updateOrPersistEntity(persitsDropMessage);
	}

	@Override
	public List<PersitsDropMessage> loadConversation(Contact contact) throws PersistenceException {
		List<PersitsDropMessage> result = new LinkedList<>();
		List<PersitsDropMessage> messages = persistence.getEntities(PersitsDropMessage.class);
		for (PersitsDropMessage d : messages) {
			if(d.getContact() == null || d.getDropMessage() == null){
				continue;
			}
			if (d.getContact().getEcPublicKey().hashCode() == contact.getEcPublicKey().hashCode()) {
				result.add(d);
			}
		}
		return result;
	}
}
