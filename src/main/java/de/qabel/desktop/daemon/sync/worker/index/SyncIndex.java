package de.qabel.desktop.daemon.sync.worker.index;

import java.nio.file.Path;

public interface SyncIndex {
    void update(Path localPath, Long localMtime, boolean exists);

    boolean isUpToDate(Path localPath, Long localMtime, boolean existing);

    boolean hasAlreadyBeenDeleted(Path localPath, Long mtime);

    SyncIndexEntry get(Path localPath);

    void clear();
}
