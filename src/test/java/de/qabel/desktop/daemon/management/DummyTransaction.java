package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class DummyTransaction extends AbstractTransaction {
    public DummyTransaction() {
        super(1000L);
    }

    @Override
    public TYPE getType() {
        return null;
    }

    @Override
    public BoxVolume getBoxVolume() {
        return null;
    }

    @Override
    public Path getSource() {
        return null;
    }

    @Override
    public Path getDestination() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public long getStagingDelayMillis() {
        return 0;
    }
}
