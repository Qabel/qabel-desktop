package de.qabel.desktop.daemon.management;

import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;

public interface Download extends Transaction<BoxPath, Path> {
    void setMtime(Long mtime);

    @Override
    void setSize(long size);

    @Override
    BoxPath getSource();
}
