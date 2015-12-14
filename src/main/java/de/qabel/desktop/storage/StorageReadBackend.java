package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageReadBackend {

	/**
	 * Download a file from the storage
	 * @param name
	 * @return
	 * @throws QblStorageException
	 */
	abstract InputStream download(String name) throws QblStorageException;
}
