package de.qabel.desktop.daemon.sync.worker.index;

import de.qabel.desktop.nio.boxfs.BoxPath;

public interface SyncIndex {
    SyncIndexEntry get(BoxPath relativePath);

    void clear();
}
