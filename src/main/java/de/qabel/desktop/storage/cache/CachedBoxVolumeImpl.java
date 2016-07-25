package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CachedBoxVolumeImpl extends BoxVolumeImpl implements CachedBoxVolume {
    private CachedIndexNavigation navigation;

    public CachedBoxVolumeImpl(StorageReadBackend readBackend, StorageWriteBackend writeBackend, QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
        super(readBackend, writeBackend, keyPair, deviceId, tempDir, prefix);
    }

    @Override
    public synchronized CachedIndexNavigation navigate() throws QblStorageException {
        if (navigation == null) {
            IndexNavigation nav;
            try {
                nav = super.navigate();
            } catch (QblStorageNotFound e) {
                createIndex(getRootRef());
                nav = super.navigate();
            }
            navigation = new CachedIndexNavigation(nav, BoxFileSystem.getRoot());
        }
        return navigation;
    }

}
