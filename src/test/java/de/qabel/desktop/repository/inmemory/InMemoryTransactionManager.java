package de.qabel.desktop.repository.inmemory;

import de.qabel.desktop.repository.RunnableTransaction;
import de.qabel.desktop.repository.Transaction;
import de.qabel.desktop.repository.TransactionManager;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.exception.TransactionException;

import java.util.concurrent.Callable;

public class InMemoryTransactionManager implements TransactionManager {
    @Override
    public Transaction beginTransaction() throws TransactionException {
        return null;
    }

    @Override
    public <T> T transactional(Callable<T> transactionBasedCallback) throws PersistenceException {
        try {
            return transactionBasedCallback.call();
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage(), e);
        }
    }

    @Override
    public void transactional(RunnableTransaction runnable) throws PersistenceException {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage(), e);
        }
    }
}
