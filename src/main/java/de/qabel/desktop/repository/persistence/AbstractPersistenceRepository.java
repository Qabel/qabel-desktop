package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;

public abstract class AbstractPersistenceRepository {
	protected Persistence<String> persistence;
	public AbstractPersistenceRepository(Persistence<String> persistence) {
		this.persistence = persistence;
	}
}
