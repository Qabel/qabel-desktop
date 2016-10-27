package de.qabel.desktop.daemon.management;

import de.qabel.box.storage.BoxVolume;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;

public class ManualDownload extends AbstractManualTransaction<BoxPath,Path> implements Download {
    public ManualDownload(long mtime, TYPE type, BoxVolume volume, BoxPath source, Path destination, boolean isDir) {
        super(mtime, isDir, destination, source, type, volume);
    }

    public ManualDownload(TYPE type, BoxVolume volume, BoxPath source, Path destination, boolean isDir) {
        super(System.currentTimeMillis(), isDir, destination, source, type, volume);
    }

    @Override
    public void setMtime(Long mtime) {
        this.mtime = mtime;
    }
}
