package de.qabel.desktop.repository;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface IdentityRepository {
	Identity find(String id) throws EntityNotFoundExcepion, PersistenceException;

	Identities findAll() throws EntityNotFoundExcepion, PersistenceException;

	void save(Identity identity) throws PersistenceException;
}
