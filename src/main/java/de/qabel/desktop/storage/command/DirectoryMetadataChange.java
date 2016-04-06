package de.qabel.desktop.storage.command;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNameConflict;
import de.qabel.desktop.storage.DirectoryMetadata;

public interface DirectoryMetadataChange<T> {
    T execute(DirectoryMetadata dm) throws QblStorageException;
}
