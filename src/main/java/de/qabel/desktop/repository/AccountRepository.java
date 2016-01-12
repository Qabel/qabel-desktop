package de.qabel.desktop.repository;

import de.qabel.core.config.Account;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface AccountRepository {
	Account find(String id) throws EntityNotFoundExcepion;

	List<Account> findAll();

	void save(Account account) throws PersistenceException;
}
