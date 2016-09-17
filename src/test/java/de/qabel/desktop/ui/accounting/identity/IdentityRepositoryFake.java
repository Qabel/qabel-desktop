package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public class IdentityRepositoryFake implements IdentityRepository {

    @Override
    public Identity find(String keyId) throws EntityNotFoundException, PersistenceException {
        throw new PersistenceException("woops");
    }

    @Override
    public Identity find(int id) throws EntityNotFoundException, PersistenceException {
        throw new PersistenceException("woops");
    }

    @Override
    public Identities findAll() throws PersistenceException {
        throw new PersistenceException("woops");
    }

    @Override
    public void save(Identity identity) throws PersistenceException {
        throw new PersistenceException("woops");
    }

    @Override
    public void delete(Identity identity) throws PersistenceException {
        throw new PersistenceException("woops");
    }
}
