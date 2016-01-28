package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.LinkedList;
import java.util.List;

public class StubContactRepository implements ContactRepository {
	private List<Contact> contacts = new LinkedList<>();


	@Override
	public void save(Contact contact) throws PersistenceException {
		contacts.add(contact);
	}

	@Override
	public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion {
		return new Contact(identity, identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
	}

	@Override
	public List<Contact> findAllContactFromOneIdentity(Identity identity) throws EntityNotFoundExcepion {
		return contacts;
	}

}
