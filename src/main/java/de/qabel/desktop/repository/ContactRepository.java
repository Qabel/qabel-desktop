package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.ContactObserver;

import java.util.List;

public interface ContactRepository {

	Contacts findContactsFromOneIdentity(Identity identity) throws EntityNotFoundExcepion;

	void save(Contact contact, Identity identity) throws PersistenceException;
	void delete(Contact contact, Identity identity) throws PersistenceException;

	Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion;


}
