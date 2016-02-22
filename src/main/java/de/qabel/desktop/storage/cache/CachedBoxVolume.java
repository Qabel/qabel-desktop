package de.qabel.desktop.storage.cache;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.storage.*;

import java.io.File;
import java.nio.file.Paths;

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
			navigation = new CachedIndexNavigation(nav, Paths.get("/"));
		}
		return navigation;
	}
}
