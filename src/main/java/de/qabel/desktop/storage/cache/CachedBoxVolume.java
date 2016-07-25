package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.exceptions.QblStorageException;

public interface CachedBoxVolume extends BoxVolume {
    @Override
    CachedIndexNavigation navigate() throws QblStorageException;
}
