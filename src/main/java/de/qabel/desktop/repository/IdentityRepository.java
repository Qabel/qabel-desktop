package de.qabel.desktop.repository;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

public interface IdentityRepository {
    /**
     * @param keyId KeyIdentifier of the Identities public key
     */
    Identity find(String keyId) throws EntityNotFoundExcepion, PersistenceException;

    Identity find(int id) throws EntityNotFoundExcepion, PersistenceException;

    Identities findAll() throws EntityNotFoundExcepion, PersistenceException;

    void save(Identity identity) throws PersistenceException;
}
