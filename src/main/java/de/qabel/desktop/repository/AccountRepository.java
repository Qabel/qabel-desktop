package de.qabel.desktop.repository;

import de.qabel.core.config.Account;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;

import java.util.List;

public interface AccountRepository {
	Account find(String id) throws EntityNotFoundExcepion;
	List<Account> findAll();
}
