package de.qabel.desktop.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;

public class FolderNavigation extends AbstractNavigation {

	private static final Logger logger = LoggerFactory.getLogger(FolderNavigation.class.getName());

	private final byte[] key;

	FolderNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] key, byte[] deviceId,
	                 StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		super(dm, keyPair, deviceId, readBackend, writeBackend);
		this.key = key;
	}

	@Override
	protected void uploadDirectoryMetadata() throws QblStorageException {
		logger.info("Uploading directory metadata");
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		uploadEncrypted(dm.getPath(), secretKey, dm.getFileName());
	}

	@Override
	protected DirectoryMetadata reloadMetadata() throws QblStorageException {
		logger.info("Reloading directory metadata");
		// duplicate of navigate()
		try {
			InputStream indexDl = readBackend.download(dm.getFileName());
			File tmp = File.createTempFile("dir", "db", dm.getTempDir());
			SecretKey key = makeKey(this.key);
			if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
				return DirectoryMetadata.openDatabase(tmp, deviceId, dm.getFileName(), dm.getTempDir());
			} else {
				throw new QblStorageNotFound("Invalid key");
			}
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}
}
