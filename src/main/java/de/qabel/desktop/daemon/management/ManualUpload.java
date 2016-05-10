package de.qabel.desktop.daemon.management;

import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Files;
import java.nio.file.Path;

public class ManualUpload extends AbstractManualTransaction<Path, BoxPath> implements Upload {

    public ManualUpload(TYPE type, BoxVolume volume, Path source, BoxPath destination) {
        this(type, volume, source, destination, Files.isDirectory(source));
    }

    public ManualUpload(TYPE type, BoxVolume volume, Path source, BoxPath destination, boolean isDir) {
        super(System.currentTimeMillis(), isDir, destination, source, type, volume);

    }
}
