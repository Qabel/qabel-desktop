package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

public interface AuthenticatedDownloader {
	StorageDownload download(String url, String ifModifiedVersion) throws QblStorageException, UnmodifiedException;
}
