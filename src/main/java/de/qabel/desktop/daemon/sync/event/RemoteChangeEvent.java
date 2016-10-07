package de.qabel.desktop.daemon.sync.event;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.ReadableBoxNavigation;

import java.nio.file.Path;

public class RemoteChangeEvent extends AbstractChangeEvent {
    private final BoxObject boxObject;
    private final ReadableBoxNavigation navigation;

    public RemoteChangeEvent(
            Path path,
            boolean isDirecotry,
            Long mtime,
            ChangeEvent.TYPE type,
            BoxObject boxObject,
            ReadableBoxNavigation navigation
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

    public ReadableBoxNavigation getBoxNavigation() {
        return navigation;
    }

    @Override
    public long getSize() {
        return boxObject instanceof BoxFile ? ((BoxFile)boxObject).getSize() : 0L;
    }
}
