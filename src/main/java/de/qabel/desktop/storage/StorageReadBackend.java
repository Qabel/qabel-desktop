package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.InputStream;

public interface StorageReadBackend {

	/**
	 * Download a file from the storage
	 */
	StorageDownload download(String name) throws QblStorageException;

	/**
	 * Download a file from the storage if it was modified (new version / etag / ...)
	 */
	StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException;

	String getUrl(String meta);
}
