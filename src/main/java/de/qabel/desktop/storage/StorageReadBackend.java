package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

public interface StorageReadBackend {

	/**
	 * Download a file from the storage
	 */
	StorageDownload download(String name) throws QblStorageException;
}
