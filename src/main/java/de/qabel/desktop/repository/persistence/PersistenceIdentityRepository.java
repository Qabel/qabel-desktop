package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public class PersistenceIdentityRepository extends AbstractPersistenceRepository implements IdentityRepository {
	public PersistenceIdentityRepository(Persistence<String> persistence) {
		super(persistence);
	}

	@Override
	public Identity find(String id) throws EntityNotFoundExcepion {
		Identity entity = persistence.getEntity(id, Identity.class);
		if (entity == null) {
			throw new EntityNotFoundExcepion("No identity found for id " + id);
		}
		return entity;
	}

	@Override
	public List<Identity> findAll() throws EntityNotFoundExcepion {
		return persistence.getEntities(Identity.class);
	}

	@Override
	public void save(Identity identity) throws PersistenceException {
		boolean result;
		try {
			result = persistence.updateOrPersistEntity(identity);
		} catch (Exception e) {
			throw new PersistenceException("Failed to save Entity " + identity + ": " + e.getMessage(), e);
		}
		if (!result) {
			throw new PersistenceException("Failed to save Entity " + identity + ", reason unknown");
		}
	}
}
