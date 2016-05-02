package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndex;

public class SqliteSyncIndexFactory implements SyncIndexFactory {
    @Override
    public SyncIndex getIndex(BoxSyncConfig config) {
        return new InMemorySyncIndex(); //XXX
    }
}
