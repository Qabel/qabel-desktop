package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public abstract class AbstractBoxSyncBasedTransaction<S extends Path, D extends Path> extends AbstractTransaction<S, D> {
    protected final BoxSyncConfig boxSyncConfig;
    protected final WatchEvent event;
    protected final BoxVolume volume;

    public AbstractBoxSyncBasedTransaction(BoxVolume volume, WatchEvent event, BoxSyncConfig boxSyncConfig) {
        super(event.getMtime());
        this.volume = volume;
        this.event = event;
        this.boxSyncConfig = boxSyncConfig;
    }

    @Override
    public Transaction.TYPE getType() {
        if (!(event instanceof ChangeEvent)) {
            return Transaction.TYPE.CREATE;
        }

        switch (((ChangeEvent) event).getType()) {
            case CREATE:
                return Transaction.TYPE.CREATE;
            case DELETE:
                return Transaction.TYPE.DELETE;
            default:
                return Transaction.TYPE.UPDATE;
        }
    }

    @Override
    public BoxVolume getBoxVolume() {
        return volume;
    }

    @Override
    public boolean isDir() {
        return event.isDir();
    }
}
