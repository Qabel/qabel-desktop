package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.IndexNavigation;
import de.qabel.box.storage.exceptions.QblStorageException;
import org.jetbrains.annotations.NotNull;

public interface CachedBoxVolume extends BoxVolume {
    @NotNull
    @Override
    IndexNavigation navigate() throws QblStorageException;
}
