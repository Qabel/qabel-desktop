package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistence;

import java.util.Observable;

public abstract class AbstractPersistenceRepository extends Observable{
	protected Persistence<String> persistence;

	public AbstractPersistenceRepository(Persistence<String> persistence) {
		this.persistence = persistence;
	}
}
