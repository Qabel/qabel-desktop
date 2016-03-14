package de.qabel.desktop.storage.cache;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.IndexNavigation;
import de.qabel.desktop.storage.StorageReadBackend;
import de.qabel.desktop.storage.StorageWriteBackend;

import java.io.File;

public class CachedBoxVolume extends BoxVolume {
	private CachedIndexNavigation navigation;

	public CachedBoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend, QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
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
