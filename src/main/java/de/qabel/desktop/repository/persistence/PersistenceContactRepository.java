package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.LinkedList;
import java.util.List;

public class PersistenceContactRepository extends AbstractCachedPersistenceRepository<Contact> implements ContactRepository {

	public PersistenceContactRepository(Persistence<String> persistence) {
		super(persistence);
	}

	@Override
	public List<Contact> findAllContactFromOneIdentity(Identity i) throws EntityNotFoundExcepion {
		List<Contact> entities = persistence.getEntities(Contact.class);
		List<Contact> contacts = new LinkedList<>();
		for (Contact c: entities){

			if(c.getContactOwner().getPrimaryKeyPair().hashCode() == i.getPrimaryKeyPair().hashCode()){
				contacts.add(c);
			}
		}
		return contacts;
	}

	@Override
	public void save(Contact contact) throws PersistenceException {
		boolean result;
		try {
			result = persistence.updateOrPersistEntity(contact);
		} catch (Exception e) {
			throw new PersistenceException("Failed to save Entity " + contact + ": " + e.getMessage(), e);
		}
		if (!result) {
			throw new PersistenceException("Failed to save Entity " + contact + ", reason unknown");
		}
	}
}
