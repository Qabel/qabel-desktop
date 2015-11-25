package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageWriteBackend {
	abstract long upload(String name, InputStream content) throws QblStorageException;
	abstract void delete(String name) throws QblStorageException;
}
