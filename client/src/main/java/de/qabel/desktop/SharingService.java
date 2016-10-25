package de.qabel.desktop;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;

public interface SharingService {
    void downloadShare(
        BoxExternalFile boxFile,
        ShareNotificationMessage message,
        Path targetFile,
        AuthenticatedDownloader downloader
    ) throws IOException, InvalidKeyException, QblStorageException, UnmodifiedException;

    BoxObject loadFileMetadata(ShareNotificationMessage message, AuthenticatedDownloader downloader) throws IOException, QblStorageException, UnmodifiedException, InvalidKeyException;


    void shareAndSendMessage(Identity sender, Contact receiver, BoxFile objectToShare, String message, BoxNavigation navigation) throws QblStorageException, PersistenceException, QblNetworkInvalidResponseException;
}
