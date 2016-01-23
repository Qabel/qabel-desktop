package de.qabel.desktop.storage;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNameConflict;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.*;

public abstract class AbstractNavigation implements BoxNavigation {
	private static final Logger logger = LoggerFactory.getLogger(AbstractNavigation.class.getName());

	DirectoryMetadata dm;
	final QblECKeyPair keyPair;
	final byte[] deviceId;
	final CryptoUtils cryptoUtils;

	final StorageReadBackend readBackend;
	final StorageWriteBackend writeBackend;

	private final Set<String> deleteQueue = new HashSet<>();
	private final Set<FileUpdate> updatedFiles = new HashSet<>();

	private boolean autocommit = true;


	AbstractNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
					   StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		this.dm = dm;
		this.keyPair = keyPair;
		this.deviceId = deviceId;
		this.readBackend = readBackend;
		this.writeBackend = writeBackend;
		cryptoUtils = new CryptoUtils();

	}

	@Override
	public synchronized AbstractNavigation navigate(BoxFolder target) throws QblStorageException {
		try {
			InputStream indexDl = readBackend.download(target.ref);
			File tmp = File.createTempFile("dir", "db", dm.getTempDir());
			KeyParameter key = new KeyParameter(target.key);
			if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
				DirectoryMetadata dm = DirectoryMetadata.openDatabase(
						tmp, deviceId, target.ref, this.dm.getTempDir());
				return new FolderNavigation(dm, keyPair, target.key, deviceId, readBackend, writeBackend);
			} else {
				throw new QblStorageNotFound("Invalid key");
			}
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public synchronized void setMetadata(DirectoryMetadata dm) {
		this.dm = dm;
	}

	@Override
	public synchronized void commit() throws QblStorageException {
		byte[] version = dm.getVersion();
		dm.commit();
		logger.info("Committing version " + new String(Hex.encodeHex(dm.getVersion()))
				+ " with device id " + new String(Hex.encodeHex(dm.deviceId)));
		DirectoryMetadata updatedDM = null;
		try {
			updatedDM = reloadMetadata();
			logger.info("Remote version is " + new String(Hex.encodeHex(updatedDM.getVersion())));
		} catch (QblStorageNotFound e) {
			logger.info("Could not reload metadata");
		}
		// the remote version has changed from the _old_ version
		if ((updatedDM != null) && (!Arrays.equals(version, updatedDM.getVersion()))) {
			logger.info("Conflicting version");
			// ignore our local directory metadata
			// all changes that are not inserted in the new dm are _lost_!
			dm = updatedDM;
			for (FileUpdate update : updatedFiles) {
				handleConflict(update);
			}
			dm.commit();
		}
		uploadDirectoryMetadata();
		for (String ref : deleteQueue) {
			writeBackend.delete(ref);
		}
		// TODO: make a test fail without these
		deleteQueue.clear();
		updatedFiles.clear();
	}

	private void handleConflict(FileUpdate update) throws QblStorageException {
		BoxFile local = update.updated;
		BoxFile newFile = dm.getFile(local.name);
		if (newFile == null) {
			try {
				dm.insertFile(local);
			} catch (QblStorageNameConflict e) {
				// name clash with a folder or external
				local.name = conflictName(local);
				// try again until we get no name clash
				handleConflict(update);
			}
		} else if (newFile.equals(update.old)) {
			logger.info("No conflict for the file " + local.name);
		} else {
			logger.info("Inserting conflict marked file");
			local.name = conflictName(local);
			if (update.old != null) {
				dm.deleteFile(update.old);
			}
			if (dm.getFile(local.name) == null) {
				dm.insertFile(local);
			}
		}
	}

	private String conflictName(BoxFile local) {
		return local.name + "_conflict_" + local.mtime.toString();
	}

	protected abstract void uploadDirectoryMetadata() throws QblStorageException;

	@Override
	public BoxNavigation navigate(BoxExternal target) {
		throw new NotImplementedException("Externals are not yet implemented!");
	}

	@Override
	public List<BoxFile> listFiles() throws QblStorageException {
		return dm.listFiles();
	}

	@Override
	public List<BoxFolder> listFolders() throws QblStorageException {
		return dm.listFolders();
	}

	@Override
	public List<BoxExternal> listExternals() throws QblStorageException {
		throw new NotImplementedException("Externals are not yet implemented!");
	}

	@Override
	public synchronized BoxFile upload(String name, File file) throws QblStorageException {
		BoxFile oldFile = dm.getFile(name);
		if (oldFile != null) {
			throw new QblStorageNameConflict("File already exists");
		}
		return uploadFile(name, file, null);
	}

	@Override
	public synchronized BoxFile overwrite(String name, File file) throws QblStorageException {
		BoxFile oldFile = dm.getFile(name);
		if (oldFile == null) {
			throw new QblStorageNotFound("Could not find file to overwrite");
		}
		dm.deleteFile(oldFile);
		return uploadFile(name, file, oldFile);
	}

	private BoxFile uploadFile(String name, File file, BoxFile oldFile) throws QblStorageException {
		KeyParameter key = cryptoUtils.generateSymmetricKey();
		String block = UUID.randomUUID().toString();
		BoxFile boxFile = new BoxFile(block, name, file.length(), 0L, key.getKey());
		boxFile.mtime = uploadEncrypted(file, key, "blocks/" + block);
		updatedFiles.add(new FileUpdate(oldFile, boxFile));
		dm.insertFile(boxFile);
		autocommit();
		return boxFile;
	}

	private void autocommit() throws QblStorageException {
		if (!autocommit) {
			return;
		}

		commit();
	}

	protected long uploadEncrypted(File file, KeyParameter key, String block) throws QblStorageException {
		try {
			File tempFile = File.createTempFile("upload", "up", dm.getTempDir());
			OutputStream outputStream = new FileOutputStream(tempFile);
			if (!cryptoUtils.encryptFileAuthenticatedSymmetric(file, outputStream, key)) {
				throw new QblStorageException("Encryption failed");
			}
			outputStream.flush();
			return writeBackend.upload(block, new FileInputStream(tempFile));
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public InputStream download(BoxFile boxFile) throws QblStorageException {
		InputStream download = readBackend.download("blocks/" + boxFile.block);
		File temp;
		KeyParameter key = new KeyParameter(boxFile.key);
		try {
			temp = File.createTempFile("upload", "down", dm.getTempDir());
			if (!cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download, temp, key)) {
				throw new QblStorageException("Decryption failed");
			}
			return new FileInputStream(temp);
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public synchronized BoxFolder createFolder(String name) throws QblStorageException {
		DirectoryMetadata dm = DirectoryMetadata.newDatabase(null, deviceId, this.dm.getTempDir());
		KeyParameter secretKey = cryptoUtils.generateSymmetricKey();
		BoxFolder folder = new BoxFolder(dm.getFileName(), name, secretKey.getKey());
		this.dm.insertFolder(folder);
		BoxNavigation newFolder = new FolderNavigation(dm, keyPair, secretKey.getKey(),
				deviceId, readBackend, writeBackend);
		newFolder.commit();
		autocommit();
		return folder;
	}

	@Override
	public synchronized void delete(BoxFile file) throws QblStorageException {
		dm.deleteFile(file);
		deleteQueue.add("blocks/" + file.block);
		autocommit();
	}

	@Override
	public synchronized void delete(BoxFolder folder) throws QblStorageException {
		BoxNavigation folderNav = navigate(folder);
		for (BoxFile file : folderNav.listFiles()) {
			logger.info("Deleting file " + file.name);
			folderNav.delete(file);
		}
		for (BoxFolder subFolder : folderNav.listFolders()) {
			logger.info("Deleting folder " + folder.name);
			folderNav.delete(subFolder);
		}
		folderNav.commit();
		dm.deleteFolder(folder);
		deleteQueue.add(folder.ref);
		autocommit();
	}

	@Override
	public void delete(BoxExternal external) throws QblStorageException {

	}

	private static class FileUpdate {
		final BoxFile old;
		final BoxFile updated;

		public FileUpdate(BoxFile old, BoxFile updated) {
			this.old = old;
			this.updated = updated;
		}

		@Override
		public int hashCode() {
			int result = old != null ? old.hashCode() : 0;
			result = 31 * result + (updated != null ? updated.hashCode() : 0);
			return result;
		}
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	@Override
	public BoxNavigation navigate(String folderName) throws QblStorageException {
		return navigate(getFolder(folderName));
	}

	@Override
	public BoxFolder getFolder(String name) throws QblStorageException {
		List<BoxFolder> folders = listFolders();
		for (BoxFolder folder : folders) {
			if (folder.name.equals(name)) {
				return folder;
			}
		}
		throw new IllegalArgumentException("no subfolder named " + name);
	}

	@Override
	public boolean hasFolder(String name) throws QblStorageException {
		try {
			getFolder(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public boolean hasFile(String name) throws QblStorageException {
		try {
			getFile(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public BoxFile getFile(String name) throws QblStorageException {
		List<BoxFile> files = listFiles();
		for (BoxFile file : files) {
			if (file.name.equals(name)) {
				return file;
			}
		}
		throw new IllegalArgumentException("no file named " + name);
	}

	@Override
	public DirectoryMetadata getMetadata() {
		return dm;
	}
}
