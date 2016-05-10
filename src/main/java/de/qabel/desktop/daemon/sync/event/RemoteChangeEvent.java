package de.qabel.desktop.daemon.sync.event;

import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxObject;

import java.nio.file.Path;

public class RemoteChangeEvent extends AbstractChangeEvent {
    private final BoxObject boxObject;
    private final BoxNavigation navigation;

    public RemoteChangeEvent(
            Path path,
            boolean isDirecotry,
            Long mtime,
            ChangeEvent.TYPE type,
            BoxObject boxObject,
            BoxNavigation navigation
    ) {
        super(path, isDirecotry, mtime, type);
        this.boxObject = boxObject;
        this.navigation = navigation;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public BoxObject getBoxObject() {
        return boxObject;
    }

    public BoxNavigation getBoxNavigation() {
        return navigation;
    }

    @Override
    public long getSize() {
        return boxObject instanceof BoxFile ? ((BoxFile)boxObject).getSize() : 0L;
    }
}
