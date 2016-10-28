package de.qabel.desktop.daemon.sync.worker.index.memory;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;

public class InMemorySyncIndexFactory implements SyncIndexFactory {
    @Override
    public SyncIndex getIndex(BoxSyncConfig config) {
        return new InMemorySyncIndex();
    }
}
