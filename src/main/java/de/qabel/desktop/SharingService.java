package de.qabel.desktop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.*;
import org.spongycastle.util.encoders.Hex;

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
