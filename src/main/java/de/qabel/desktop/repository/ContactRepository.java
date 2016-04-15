package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

public interface ContactRepository {

    Contacts find(Identity identity) throws PersistenceException;

    void save(Contact contact, Identity identity) throws PersistenceException;

    void delete(Contact contact, Identity identity) throws PersistenceException;

    Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundExcepion;
}
