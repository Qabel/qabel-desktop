package de.qabel.desktop.daemon.sync.worker.index;

import de.qabel.desktop.nio.boxfs.BoxPath;

import java.io.Serializable;
import java.nio.file.Path;

public class SyncIndexEntry implements Serializable {
    private BoxPath relativePath;
    private SyncState syncedState;
    private SyncState localState = new SyncState();
    private SyncState remoteState = new SyncState();

    public SyncIndexEntry(BoxPath relativePath) {
        this(relativePath, new SyncState());
    }

    public SyncIndexEntry(BoxPath relativePath, SyncState syncedState) {
        this.relativePath = relativePath;
        this.syncedState = syncedState;
    }

    public BoxPath getRelativePath() {
        return relativePath;
    }

    public SyncState getSyncedState() {
        return syncedState;
    }

    public SyncState getLocalState() {
        return localState;
    }

    public SyncState getRemoteState() {
        return remoteState;
    }

    public void setSyncedState(SyncState syncedState) {
        this.syncedState = syncedState;
    }

    public void setLocalState(SyncState localState) {
        this.localState = localState;
    }

    public void setRemoteState(SyncState remoteState) {
        this.remoteState = remoteState;
    }
}
