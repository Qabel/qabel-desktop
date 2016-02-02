package de.qabel.desktop.storage;

import com.amazonaws.util.IOUtils;
import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public class IndexNavigation extends AbstractNavigation {
	private Map<Integer, String> directoryMetadataMHashes = new WeakHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(IndexNavigation.class.getSimpleName());

	IndexNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
					StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		super(dm, keyPair, deviceId, readBackend, writeBackend);
	}

	@Override
	public DirectoryMetadata reloadMetadata() throws QblStorageException {
		// TODO: duplicate with BoxVoume.navigate()
		String rootRef = dm.getFileName();

		try {
			StorageDownload download = readBackend.download(rootRef, directoryMetadataMHashes.get(Arrays.hashCode(dm.getVersion())));
			InputStream indexDl = download.getInputStream();
			File tmp;
			byte[] encrypted = IOUtils.toByteArray(indexDl);
			DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
			tmp = File.createTempFile("dir", "db", dm.getTempDir());
			tmp.deleteOnExit();
			logger.info("Using " + tmp.toString() + " for the metadata file");
			OutputStream out = new FileOutputStream(tmp);
			out.write(plaintext.getPlaintext());
			out.close();
			DirectoryMetadata newDm = DirectoryMetadata.openDatabase(tmp, deviceId, rootRef, dm.getTempDir());
			directoryMetadataMHashes.put(Arrays.hashCode(newDm.getVersion()), download.getMHash());
			return newDm;
		} catch (UnmodifiedException e) {
			return dm;
		} catch (IOException | InvalidCipherTextException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	protected void uploadDirectoryMetadata() throws QblStorageException {
		try {
			byte[] plaintext = IOUtils.toByteArray(new FileInputStream(dm.path));
			byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
			writeBackend.upload(dm.getFileName(), new ByteArrayInputStream(encrypted));
			logger.info("Uploading metadata file with name " + dm.getFileName());
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}
}
