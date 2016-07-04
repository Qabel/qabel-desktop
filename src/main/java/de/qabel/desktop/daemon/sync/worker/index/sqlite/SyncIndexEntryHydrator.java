package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.repository.GenericEntityManager;
import de.qabel.core.repository.sqlite.hydrator.AbstractHydrator;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SyncIndexEntryHydrator extends AbstractHydrator<SyncIndexEntry> {
    private GenericEntityManager<BoxPath, SyncIndexEntry> em;

    public SyncIndexEntryHydrator(GenericEntityManager<BoxPath, SyncIndexEntry> em) {
        this.em = em;
    }

    @Override
    protected String[] getFields() {
        return new String[]{"relative_path", "existing", "mtime", "size"};
    }

    @Override
    public SyncIndexEntry hydrateOne(ResultSet resultSet) throws SQLException {
        int i = 0;
        BoxPath relativePath = BoxFileSystem.get(resultSet.getString(++i));
        if (em.contains(SyncIndexEntry.class, relativePath)) {
            return em.get(SyncIndexEntry.class, relativePath);
        }

        return new SyncIndexEntry(
            relativePath,
            new SyncState(
                resultSet.getBoolean(++i),
                resultSet.getLong(++i),
                resultSet.getLong(++i)
            )
        );
    }

    @Override
    public void recognize(SyncIndexEntry instance) {
        em.put(SyncIndexEntry.class, instance);
    }
}
