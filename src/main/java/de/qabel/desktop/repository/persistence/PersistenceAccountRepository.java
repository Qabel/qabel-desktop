package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class PersistenceAccountRepository extends AbstractCachedPersistenceRepository<Account> implements AccountRepository {
    public PersistenceAccountRepository(Persistence<String> persistence) {
        super(persistence);
    }

    @Override
    public Account find(String id) throws EntityNotFoundExcepion {
        if (isCached(id)) {
            return fromCache(id);
        }
        Account entity = persistence.getEntity(id, Account.class);
        if (entity == null) {
            throw new EntityNotFoundExcepion("No Account found for id " + id);
        }
        cache(entity);
        return entity;
    }

    @Override
    public Account find(int id) throws EntityNotFoundExcepion {
        throw new NotImplementedException();
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = persistence.getEntities(Account.class);
        syncWithCache(accounts);
        return accounts;
    }

    @Override
    public void save(Account account) throws PersistenceException {
        boolean result;
        try {
            result = persistence.updateOrPersistEntity(account);
        } catch (Exception e) {
            throw new PersistenceException("Failed to save account " + account.getPersistenceID() + ": " + e.getMessage(), e);
        }
        if (!result) {
            throw new PersistenceException("Failed to save Entity " + account + ", reason unknown");
        }
        cache(account);
    }
}
