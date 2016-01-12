package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistence;

public abstract class AbstractPersistenceRepository {
	protected Persistence<String> persistence;

	public AbstractPersistenceRepository(Persistence<String> persistence) {
		this.persistence = persistence;
	}
}
