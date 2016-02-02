package de.qabel.desktop.storage.cache;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.StorageReadBackend;
import de.qabel.desktop.storage.StorageWriteBackend;

import java.io.File;
import java.nio.file.Paths;

public class CachedBoxVolume extends BoxVolume {
	private CachedBoxNavigation navigation;

	public CachedBoxVolume(String bucket, String prefix, AWSCredentials credentials, QblECKeyPair keyPair, byte[] deviceId, File tempDir) {
		super(bucket, prefix, credentials, keyPair, deviceId, tempDir);
	}

	public CachedBoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend, QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
		super(readBackend, writeBackend, keyPair, deviceId, tempDir, prefix);
	}

	@Override
	public synchronized CachedBoxNavigation navigate() throws QblStorageException {
		if (navigation == null) {
			BoxNavigation nav;
			try {
				nav = super.navigate();
			} catch (QblStorageNotFound e) {
				createIndex(getRootRef());
				nav = super.navigate();
			}
			navigation = new CachedBoxNavigation(nav, Paths.get("/"));
		}
		return navigation;
	}
}
