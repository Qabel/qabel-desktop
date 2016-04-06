package de.qabel.desktop.daemon.sync.event;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WatchRegisteredEvent extends AbstractWatchEvent {
    public WatchRegisteredEvent(Path path) throws IOException {
        super(path, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis());
    }

    @Override
    public boolean isValid() {
        try {
            return getPath().toFile().exists() && Files.getLastModifiedTime(getPath()).toMillis() == getMtime();
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass().getSimpleName()).warn("failed to test mtimes: " + e.getMessage(), e);
            return false;
        }
    }
}
