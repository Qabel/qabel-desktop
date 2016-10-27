package de.qabel.desktop.daemon.sync.worker.index;

import de.qabel.desktop.config.BoxSyncConfig;

public interface SyncIndexFactory {
    SyncIndex getIndex(BoxSyncConfig config);
}
