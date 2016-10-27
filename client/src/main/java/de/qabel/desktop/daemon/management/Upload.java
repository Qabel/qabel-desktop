package de.qabel.desktop.daemon.management;

import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;

public interface Upload extends Transaction<Path, BoxPath> {
}
