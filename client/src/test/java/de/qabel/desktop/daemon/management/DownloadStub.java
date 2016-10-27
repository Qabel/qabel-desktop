package de.qabel.desktop.daemon.management;

import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;

public class DownloadStub extends TransactionStub<BoxPath, Path> implements Download {
    @Override
    public void setMtime(Long mtime) {

    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }
}
