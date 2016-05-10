package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SqliteDropStateRepositoryTest extends AbstractSqliteRepositoryTest<SqliteDropStateRepository> {

    @Override
    protected SqliteDropStateRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        return new SqliteDropStateRepository(clientDatabase);
    }

    @Test(expected = EntityNotFoundException.class)
    public void throwsExceptionIfNoStateWasFound() throws Exception {
        repo.getDropState("not existing");
    }

    @Test
    public void knowsSavedDrops() throws Exception {
        repo.setDropState("drop", "state");
        assertEquals("state", repo.getDropState("drop"));
    }
}
