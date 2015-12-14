package de.qabel.desktop.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageDecryptionFailed;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageIOFailure;
import de.qabel.desktop.exceptions.QblStorageInvalidKey;
import org.apache.commons.io.IOUtils;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class BoxVolume {

	private static final Logger logger = LoggerFactory.getLogger(BoxVolume.class.getName());

	StorageReadBackend readBackend;
	StorageWriteBackend writeBackend;

	private QblECKeyPair keyPair;
	private byte[] deviceId;
	private CryptoUtils cryptoUtils;
	private File tempDir;
	private String prefix;

	public BoxVolume(String bucket, String prefix, AWSCredentials credentials,
	                 QblECKeyPair keyPair, byte[] deviceId, File tempDir) {
		this(new S3ReadBackend(bucket, prefix), new S3WriteBackend(credentials, bucket, prefix),
				keyPair, deviceId, tempDir, prefix);
	}

	public BoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend,
	                 QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
		this.keyPair = keyPair;
		this.deviceId = deviceId;
		this.readBackend = readBackend;
		this.writeBackend = writeBackend;
		cryptoUtils = new CryptoUtils();
		this.tempDir = tempDir;
		this.prefix = prefix;
		try {
			loadDriver();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	protected void loadDriver() throws ClassNotFoundException {
		logger.info("Loading PC sqlite driver");
		Class.forName("org.sqlite.JDBC");
	}

	public BoxNavigation navigate() throws QblStorageException {
		String rootRef = getRootRef();
		logger.info("Navigating to " + rootRef);
		InputStream indexDl = readBackend.download(rootRef);
		File tmp;
		try {
			byte[] encrypted = IOUtils.toByteArray(indexDl);
			DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
			tmp = File.createTempFile("dir", "db", tempDir);
			OutputStream out = new FileOutputStream(tmp);
			out.write(plaintext.getPlaintext());
			out.close();
		} catch (InvalidCipherTextException | InvalidKeyException e) {
			throw new QblStorageDecryptionFailed(e);
		} catch (IOException e) {
			throw new QblStorageIOFailure(e);
		}
		DirectoryMetadata dm = DirectoryMetadata.openDatabase(tmp, deviceId, rootRef, tempDir);
		return new IndexNavigation(dm, keyPair, deviceId, readBackend, writeBackend);
	}

	public String getRootRef() throws QblStorageException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new QblStorageException(e);
		}
		md.update(this.prefix.getBytes());
		md.update(keyPair.getPrivateKey());
		byte[] digest = md.digest();
		byte[] firstBytes = Arrays.copyOfRange(digest, 0, 16);
		ByteBuffer bb = ByteBuffer.wrap(firstBytes);
		UUID uuid = new UUID(bb.getLong(), bb.getLong());
		return uuid.toString();
	}

	public void createIndex(String bucket, String prefix) throws QblStorageException {
		createIndex("https://" + bucket + ".s3.amazonaws.com/" + prefix);
	}

	public void createIndex(String root) throws QblStorageException {
		String rootRef = getRootRef();
		DirectoryMetadata dm = DirectoryMetadata.newDatabase(root, deviceId, tempDir);
		try {
			byte[] plaintext = IOUtils.toByteArray(new FileInputStream(dm.path));
			byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
			writeBackend.upload(rootRef, new ByteArrayInputStream(encrypted));
		} catch (IOException e) {
			throw new QblStorageIOFailure(e);
		} catch (InvalidKeyException e) {
			throw new QblStorageInvalidKey(e);
		}


	}
}
