package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface ContactRepository {

	List<Contact> findAllContactFormOneIdentity(Identity identity) throws EntityNotFoundExcepion;

	void save(Contact contact) throws PersistenceException;
}
