package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceContactRepository extends AbstractCachedPersistenceRepository<Contact> implements ContactRepository {
	public PersistenceContactRepository(Persistence<String> persistence) {
		super(persistence);
	}

	private Map<String, Contacts> contacts = new HashMap<>();

	@Override
	public synchronized Contacts findContactsFromOneIdentity(Identity i) throws EntityNotFoundExcepion {

		if (contacts.containsKey(i.getKeyIdentifier())) {
			return contacts.get(i.getKeyIdentifier());
		}

		List<Contacts> cl = persistence.getEntities(Contacts.class);
		for (Contacts c : cl) {
			if (c.getIdentity().getKeyIdentifier().equals(i.getKeyIdentifier())) {
				contacts.put(i.getKeyIdentifier(), c);
				return contacts.get(i.getKeyIdentifier());

			}
		}

		Contacts newContacts = new Contacts(i);
		contacts.put(i.getKeyIdentifier(), newContacts);
		return contacts.get(i.getKeyIdentifier());

	}

	@Override
	public void save(Contact contact, Identity identity) throws PersistenceException {
		boolean result;
		try {
			Contacts personalContacts = contacts.get(identity.getKeyIdentifier());
			personalContacts.put(contact);
			result = persistence.updateOrPersistEntity(personalContacts);
		} catch (Exception e) {
			throw new PersistenceException("Failed to save Entity " + contact + ": " + e.getMessage(), e);
		}
		if (!result) {
			throw new PersistenceException("Failed to save Entity " + contact + ", reason unknown");
		}
	}

	@Override
	public void delete(Contact contact, Identity identity) throws PersistenceException {
		try {
			Contacts personalContacts = contacts.get(identity.getKeyIdentifier());
			personalContacts.remove(contact);

			if (!persistence.updateOrPersistEntity(personalContacts)) {
				throw new PersistenceException("Failed to delete Entity " + contact + ", reason unknown");
			}
		} catch (Exception e) {
			throw new PersistenceException("Failed to delete Entity " + contact + ": " + e.getMessage(), e);
		}

	}

	@Override
	public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion {

		Contacts contacts = findContactsFromOneIdentity(identity);
		Contact contact = contacts.getByKeyIdentifier(keyId);
		if (contact == null) {
			throw new EntityNotFoundExcepion("No contact with keyId " + keyId + " found");
		}
		return contact;
	}
}
