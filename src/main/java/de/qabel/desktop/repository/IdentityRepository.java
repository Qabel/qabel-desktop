package de.qabel.desktop.repository;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;

public interface IdentityRepository {
    /**
     * @param keyId KeyIdentifier of the Identities public key
     */
    Identity find(String keyId) throws EntityNotFoundException, PersistenceException;

    Identity find(int id) throws EntityNotFoundException, PersistenceException;

    Identities findAll() throws EntityNotFoundException, PersistenceException;

    void save(Identity identity) throws PersistenceException;
}
