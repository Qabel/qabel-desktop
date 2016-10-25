package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.BoxVolumeImpl;
import de.qabel.box.storage.IndexNavigation;
import de.qabel.box.storage.StorageReadBackend;
import de.qabel.box.storage.StorageWriteBackend;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.crypto.QblECKeyPair;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CachedBoxVolumeImpl extends BoxVolumeImpl implements CachedBoxVolume {
    private IndexNavigation navigation;

    public CachedBoxVolumeImpl(StorageReadBackend readBackend, StorageWriteBackend writeBackend, QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
        super(readBackend, writeBackend, keyPair, deviceId, tempDir, prefix);
    }

    @NotNull
    @Override
    public synchronized IndexNavigation navigate() throws QblStorageException {
        if (navigation == null) {
            try {
                navigation = super.navigate();
            } catch (QblStorageNotFound e) {
                createIndex(getRootRef());
                navigation = super.navigate();
            }
        }
        return navigation;
    }

}
