package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PersistenceAccountRepositoryTest extends AbstractPersistenceRepositoryTest<PersistenceAccountRepository> {
    @Override
    protected PersistenceAccountRepository createRepository(Persistence<String> persistence) {
        return new PersistenceAccountRepository(persistence);
    }

    @Test(expected = EntityNotFoundExcepion.class)
    public void throwsEntityNotFoundExceptionIfSingleEntityIsNotFound() throws Exception {
        repo.find("1");
    }

    @Test
    public void findsPersistedEntity() throws Exception {
        Account account = new Account("a", "b", "c");
        persistence.persistEntity(account);
        Account loadedAccount = repo.find(account.getPersistenceID());
        assertEquals(account, loadedAccount);
    }

    @Test
    public void findAllReturnsEmptyListByDefault() {
        List<Account> accounts = repo.findAll();
        assertEquals(0, accounts.size());
    }

    @Test
    public void findsAllPersistedEntities() {
        Account account = new Account("a", "b", "c");
        persistence.persistEntity(account);
        List<Account> accounts = repo.findAll();
        assertEquals(1, accounts.size());
        assertEquals(account, accounts.get(0));
    }
}
