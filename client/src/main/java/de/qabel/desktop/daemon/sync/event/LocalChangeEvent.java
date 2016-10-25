package de.qabel.desktop.daemon.sync.event;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;

public class LocalChangeEvent extends AbstractChangeEvent {
    public LocalChangeEvent(Path path, ChangeEvent.TYPE type) throws IOException {
        this(path, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis(), type);
    }

    public LocalChangeEvent(Path path, boolean isDir, Long mtime, ChangeEvent.TYPE type) {
        super(path, isDir, mtime, type);
    }

    @Override
    public boolean isValid() {
        try {
            long currentMtime = Files.getLastModifiedTime(getPath()).toMillis();
            boolean valid = currentMtime == getMtime();
            if (!valid) {
                LoggerFactory.getLogger(getClass()).debug("event invalid: mtime " + currentMtime + " != " + getMtime() + " on " + getPath());
            }
            return valid;
        } catch (IOException e) {
            return type == DELETE;
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.isDirectory(getPath()) ? 0L : Files.size(getPath());
        } catch (IOException e) {
            return 0L;
        }
    }
}
