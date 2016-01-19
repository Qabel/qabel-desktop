package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import java.util.LinkedList;
import java.util.List;

public class InMemoryContactRepository implements ContactRepository {
	private List<Contact> contacts = new LinkedList<>();


	@Override
	public void save(Contact contact) throws PersistenceException {
		contacts.add(contact);
	}

	@Override
	public List<Contact> findAllContactFormOneIdentity(Identity identity) throws EntityNotFoundExcepion {
		return contacts;
	}

}
