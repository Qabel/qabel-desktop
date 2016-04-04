package de.qabel.desktop.storage;

import java.nio.file.Path;

public interface PathNavigation {
    Path getPath();

    Path getPath(BoxObject folder);
}
