package de.qabel.desktop.repository;

import de.qabel.core.config.Account;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface AccountRepository {
    /**
     * binds to Persistence specific UUID persistenceID. Use find(int id) instead or just findAll()
     */
    @Deprecated
    Account find(String id) throws EntityNotFoundException;

    Account find(int id) throws EntityNotFoundException;

    List<Account> findAll() throws PersistenceException;

    void save(Account account) throws PersistenceException;
}
