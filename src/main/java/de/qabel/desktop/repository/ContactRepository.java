package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface ContactRepository {

	List<Contact> findAllContactFromOneIdentity(Identity identity) throws EntityNotFoundExcepion;

	void save(Contact contact) throws PersistenceException;

	Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion;
}
