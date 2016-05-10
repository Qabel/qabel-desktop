package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.sqlite.migration.AbstractSqliteTest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class SqliteSyncIndexTest extends AbstractSqliteTest {
    @Test
    public void persistsStatesThroughSessions() throws Exception {
        SyncIndex index = new SqliteSyncIndex(connection);
        SyncIndexEntry entry = index.get(BoxFileSystem.getRoot().resolve("testpath"));

        entry.setSyncedState(new SyncState(true, 10L, 20L));

        SyncIndex index2 = new SqliteSyncIndex(connection);
        SyncIndexEntry entry2 = index2.get(BoxFileSystem.getRoot().resolve("testpath"));
        SyncState state = entry2.getSyncedState();
        assertThat(state.isExisting(), is(true));
        assertThat(state.getMtime(), is(10L));
        assertThat(state.getSize(), is(20L));
    }
}
