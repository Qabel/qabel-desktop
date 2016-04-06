package de.qabel.desktop.repository.persistence;

import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;

import java.io.Serializable;

public class PersistentBoxSyncConfig implements Serializable {
    private static final long serialVersionUID = -2257166187751385837L;
    public String localPath;
    public String remotePath;
    public String identity;
    public String account;
    public boolean paused;
    public String name;
    public SyncIndex syncIndex;
}
