package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;


public class StubContactRepository implements ContactRepository {
	private Contacts contacts = new Contacts(new Identity("i", null, new QblECKeyPair()));


	@Override
	public void save(Contact contact, Identity identity) throws PersistenceException {
		contacts.put(contact);
	}

	@Override
	public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion {
		return new Contact(identity.getAlias(), identity.getDropUrls(), identity.getEcPublicKey());
	}

	@Override
	public Contacts findContactsFromOneIdentity(Identity identity) throws EntityNotFoundExcepion {
		return contacts;
	}


}
