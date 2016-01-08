package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistable;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import static org.junit.Assert.*;

import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import org.junit.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PersistenceIdentityRepositoryTest extends AbstractPersistenceRepositoryTest<PersistenceIdentityRepository> {

	@Test
	public void returnsEmptyListWithoutInstances() throws Exception {
		List<Identity> results = repo.findAll();
		assertEquals(0, results.size());
	}

	@Test
	public void returnsPersistedEntities() throws Exception {
		Identity identity = new Identity("a", new LinkedList<>(), null);
		persistence.persistEntity(identity);
		List<Identity> results = repo.findAll();

		assertEquals(1, results.size());
		assertEquals(identity, results.get(0));
	}

	@Test(expected = EntityNotFoundExcepion.class)
	public void throwsExcetionIfEntityIsNotFound() throws Exception {
		repo.find("1");
	}

	@Test
	public void findsPersistedEntity() throws Exception {
		Identity identity = new Identity("a", new LinkedList<>(), null);
		persistence.persistEntity(identity);

		Identity id = repo.find(identity.getPersistenceID());
		assertEquals(identity, id);
	}

	@Override
	protected PersistenceIdentityRepository createRepository(Persistence<String> persistence) {
		return new PersistenceIdentityRepository(persistence);
	}

	@Test
	public void savesNewInstances() throws Exception {
		Identity identity = new Identity("a", null, null);
		repo.save(identity);
		assertEquals(identity, repo.find(identity.getPersistenceID()));
	}

	@Test
	public void updatesExistingInstances() throws Exception {
		Identity identity = new Identity("a", null, null);
		repo.save(identity);
		String persistenceID = identity.getPersistenceID();

		identity.setAlias("b");
		repo.save(identity);

		assertEquals(identity, repo.find(persistenceID));
	}

	@Test(expected = PersistenceException.class)
	public void throwsPersistenceExceptionOnFailure() throws Exception {
		repo.save(null);
	}
}
