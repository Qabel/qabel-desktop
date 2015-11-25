package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageReadBackend {

	abstract InputStream download(String name) throws QblStorageException;
}
