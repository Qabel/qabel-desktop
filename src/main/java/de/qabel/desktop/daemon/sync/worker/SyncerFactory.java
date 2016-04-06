package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;

public interface SyncerFactory {
    Syncer factory(BoxSyncConfig config);
}
