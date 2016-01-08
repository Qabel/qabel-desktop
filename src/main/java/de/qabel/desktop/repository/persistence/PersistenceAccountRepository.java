package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;

import java.util.List;

public class PersistenceAccountRepository extends AbstractPersistenceRepository implements AccountRepository {
	public PersistenceAccountRepository(Persistence<String> persistence) {
		super(persistence);
	}

	@Override
	public Account find(String id) throws EntityNotFoundExcepion {
		Account entity = persistence.getEntity(id, Account.class);
		if (entity == null) {
			throw new EntityNotFoundExcepion("No Account found for id " + id);
		}
		return entity;
	}

	@Override
	public List<Account> findAll() {
		return persistence.getEntities(Account.class);
	}
}
