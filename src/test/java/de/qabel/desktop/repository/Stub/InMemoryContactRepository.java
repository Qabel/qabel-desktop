package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.HashMap;


public class InMemoryContactRepository implements ContactRepository {
	private HashMap<String, Contacts> contactsMap = new HashMap<>();


	@Override
	public void save(Contact contact, Identity identity) throws PersistenceException {
		Contacts contacts = find(identity);
		if(contacts == null){
			contacts = new Contacts(identity);
		}
		contacts.put(contact);
		contactsMap.put(identity.getKeyIdentifier(), contacts);
	}

	@Override
	public void delete(Contact contact, Identity identity) throws PersistenceException {
		Contacts contacts = find(identity);
		contacts.remove(contact);
	}

	@Override
	public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion {
		Contacts contacts = find(identity);
		return contacts.getByKeyIdentifier(keyId);
	}

	@Override
	public Contacts find(Identity identity) {
		Contacts contacts = contactsMap.get(identity.getKeyIdentifier());
		if(contacts == null){
			contacts = new Contacts(identity);
			contactsMap.put(identity.getKeyIdentifier(), contacts);
		}
		return contactsMap.get(identity.getKeyIdentifier());
	}


}
