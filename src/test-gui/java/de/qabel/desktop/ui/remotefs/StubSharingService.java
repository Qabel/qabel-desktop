package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.*;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;

public class StubSharingService implements SharingService {
	public DownloadRequest downloadedFile;
	public BoxObject loadFileMetadata;
	public ShareRequest shared;

	@Override
	public void downloadShare(BoxExternalFile boxFile, ShareNotificationMessage message, Path targetFile, AuthenticatedDownloader downloader) throws IOException, InvalidKeyException, QblStorageException, UnmodifiedException {
		downloadedFile = new DownloadRequest(
				boxFile,
				message,
				targetFile,
				downloader
		);
	}

	@Override
	public BoxObject loadFileMetadata(ShareNotificationMessage message, AuthenticatedDownloader downloader) throws IOException, QblStorageException, UnmodifiedException, InvalidKeyException {
		return loadFileMetadata;
	}

	@Override
	public void shareAndSendMessage(Identity sender, Contact receiver, BoxFile objectToShare, String message, BoxNavigation navigation) throws QblStorageException, PersistenceException, QblNetworkInvalidResponseException {
		navigation.share(sender.getEcPublicKey(), objectToShare, receiver.getKeyIdentifier());
		shared = new ShareRequest(
				sender,
				receiver,
				objectToShare,
				message,
				navigation
		);
	}

	private class DownloadRequest {
		public BoxExternalFile boxFile;
		public ShareNotificationMessage message;
		public Path targetFile;
		public AuthenticatedDownloader downloader;

		public DownloadRequest(BoxExternalFile boxFile, ShareNotificationMessage message, Path targetFile, AuthenticatedDownloader downloader) {
			this.boxFile = boxFile;
			this.message = message;
			this.targetFile = targetFile;
			this.downloader = downloader;
		}
	}

	public class ShareRequest {
		public Identity sender;
		public Contact receiver;
		public BoxFile objectToShare;
		public String message;
		public BoxNavigation navigation;

		public ShareRequest(Identity sender, Contact receiver, BoxFile objectToShare, String message, BoxNavigation navigation) {
			this.sender = sender;
			this.receiver = receiver;
			this.objectToShare = objectToShare;
			this.message = message;
			this.navigation = navigation;
		}
	}
}
