package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Account;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.LinkedList;
import java.util.List;

public class InMemoryAccountRepository implements AccountRepository {
    private List<Account> accounts = new LinkedList<>();

    @Override
    public Account find(String id) throws EntityNotFoundExcepion {
        for (Account account : accounts) {
            if (id.equals(String.valueOf(account.getId()))) {
                return account;
            }
        }
        throw new EntityNotFoundExcepion("no account for id " + id);
    }

    @Override
    public Account find(int id) throws EntityNotFoundExcepion {
        return find(String.valueOf(id));
    }

    @Override
    public List<Account> findAll() throws PersistenceException {
        return accounts;
    }

    @Override
    public void save(Account account) throws PersistenceException {
        if (account.getId() == 0) {
            account.setId(accounts.size() + 1);
        }
        if (!accounts.contains(account)) {
            accounts.add(account);
        }
    }
}
