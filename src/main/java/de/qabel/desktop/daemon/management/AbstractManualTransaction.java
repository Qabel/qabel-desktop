package de.qabel.desktop.daemon.management;

import de.qabel.box.storage.BoxVolume;

import java.nio.file.Path;

public abstract class AbstractManualTransaction<S extends Path, D extends Path> extends AbstractTransaction<S,D> {
    protected final TYPE type;
    protected final BoxVolume volume;
    protected final S source;
    protected final D destination;
    protected final boolean isDir;

    public AbstractManualTransaction(Long mtime, boolean isDir, D destination, S source, TYPE type, BoxVolume volume) {
        super(mtime);
        this.isDir = isDir;
        this.destination = destination;
        this.source = source;
        this.type = type;
        this.volume = volume;
    }

    @Override
    public TYPE getType() {
        return type;
    }

    @Override
    public BoxVolume getBoxVolume() {
        return volume;
    }

    @Override
    public S getSource() {
        return source;
    }

    @Override
    public D getDestination() {
        return destination;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isDir() {
        return isDir;
    }

    @Override
    public long getStagingDelayMillis() {
        return 0;
    }
}
