package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageWriteBackend {
	/**
	 * Upload a file to the storage. Will overwrite if the file exists
	 * @param name
	 * @param content
	 * @return
	 * @throws QblStorageException
	 */
	abstract long upload(String name, InputStream content) throws QblStorageException;

	/**
	 * Delete a file on the storage. Will not fail if the file was not found
	 * @param name
	 * @throws QblStorageException
	 */
	abstract void delete(String name) throws QblStorageException;
}
