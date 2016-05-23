package de.qabel.desktop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.*;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;

public class StubSharingService implements SharingService {
    public BoxFile share = new BoxFile("sharePrefix", "shareBlock", "sharedFile", 0L, 0L, "key".getBytes());
    public ShareNotificationMessage usedShareMessage;
    public QblStorageException storageException;
    public IOException ioException;

    @Override
    public void downloadShare(
        BoxExternalFile boxFile,
        ShareNotificationMessage message,
        Path targetFile,
        AuthenticatedDownloader downloader
    ) throws IOException, InvalidKeyException, QblStorageException, UnmodifiedException {
        usedShareMessage = message;
    }

    @Override
    public BoxObject loadFileMetadata(ShareNotificationMessage message, AuthenticatedDownloader downloader)
        throws IOException, QblStorageException, UnmodifiedException, InvalidKeyException {
        usedShareMessage = message;
        if (storageException != null) {
            throw storageException;
        }
        if (ioException != null) {
            throw ioException;
        }
        return share;
    }

    @Override
    public void shareAndSendMessage(
        Identity sender,
        Contact receiver,
        BoxFile objectToShare,
        String message,
        BoxNavigation navigation
    ) throws QblStorageException, PersistenceException, QblNetworkInvalidResponseException {
    }
}
