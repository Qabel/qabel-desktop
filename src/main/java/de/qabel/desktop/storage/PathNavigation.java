package de.qabel.desktop.storage;

import de.qabel.box.storage.BoxObject;

import java.nio.file.Path;

@Deprecated
public interface PathNavigation {
    @Deprecated
    Path getDesktopPath();

    @Deprecated
    Path getDesktopPath(BoxObject folder);
}
