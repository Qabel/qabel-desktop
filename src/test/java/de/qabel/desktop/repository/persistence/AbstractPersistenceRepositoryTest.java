package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public abstract class AbstractPersistenceRepositoryTest<T> {
	protected T repo;
	protected Persistence<String> persistence;
	private File dbFile;

	@Before
	public void setUp() throws Exception {
		String tmpDir = System.getProperty("java.io.tmp");
		dbFile = new File(tmpDir, "qabel-desktop.sqlite");
		if (dbFile.exists()) {
			dbFile.delete();
		}
		persistence = new SQLitePersistence(dbFile.getAbsolutePath(), "qabel".toCharArray());
		repo = createRepository(persistence);
	}

	protected abstract T createRepository(Persistence<String> persistence);

	@After
	public void tearDown() throws Exception {
		if (dbFile.exists()) {
			dbFile.delete();
		}
	}
}
