package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.*;

public class InMemoryIdentityRepository implements IdentityRepository {
	private Map<String, Identity> identities = new HashMap<>();

	@Override
	public Identity find(String id) throws EntityNotFoundExcepion {
		if (!identities.containsKey(id)) {
			throw new EntityNotFoundExcepion("id " + id + " not found");
		}
		return identities.get(id);
	}

	@Override
	public List<Identity> findAll() throws EntityNotFoundExcepion {
		List<Identity> result = new ArrayList<>();
		result.addAll(identities.values());
		return result;
	}

	@Override
	public void save(Identity identity) throws PersistenceException {
		identities.put(identity.getPersistenceID(), identity);
	}
}
