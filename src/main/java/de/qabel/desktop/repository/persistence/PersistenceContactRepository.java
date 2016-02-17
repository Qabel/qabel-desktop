package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public class PersistenceContactRepository extends AbstractCachedPersistenceRepository<Contact> implements ContactRepository {
	public PersistenceContactRepository(Persistence<String> persistence) {
		super(persistence);
	}

	private Contacts contacts;

	@Override
	public Contacts findContactsFromOneIdentity(Identity i) throws EntityNotFoundExcepion {
		if (contacts != null) {
			return contacts;
		}

		List<Contacts> cl = persistence.getEntities(Contacts.class);
		if (cl != null) {
			for (Contacts c : cl) {
				if (c.getIdentity().getEcPublicKey().hashCode() == i.getEcPublicKey().hashCode()) {
					contacts = c;
					return c;
				}
			}
		}

		contacts = new Contacts(i);
		persistence.updateOrPersistEntity(contacts);
		return contacts;
	}

	@Override
	public void save(Contact contact) throws PersistenceException {
		boolean result;
		try {
			contacts.put(contact);
			result = persistence.updateOrPersistEntity(contacts);
		} catch (Exception e) {
			throw new PersistenceException("Failed to save Entity " + contact + ": " + e.getMessage(), e);
		}
		if (!result) {
			throw new PersistenceException("Failed to save Entity " + contact + ", reason unknown");
		}
	}

	@Override
	public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion {

		Contacts contacts = findContactsFromOneIdentity(identity);
		return contacts.getByKeyIdentifier(keyId);
	}
}
