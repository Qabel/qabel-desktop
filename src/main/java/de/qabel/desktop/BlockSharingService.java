package de.qabel.desktop;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.connector.DropConnector;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;

@Deprecated
public class BlockSharingService implements SharingService {
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private DropMessageRepository dropMessageRepository;
    private DropConnector dropConnector;

    public BlockSharingService(DropMessageRepository dropMessageRepository, DropConnector dropConnector) {
        this.dropMessageRepository = dropMessageRepository;
        this.dropConnector = dropConnector;
    }

    @Override
    public void downloadShare(
        BoxExternalFile boxFile,
        ShareNotificationMessage message,
        Path targetFile,
        AuthenticatedDownloader downloader
    ) throws IOException, InvalidKeyException, QblStorageException, UnmodifiedException {
        try {
            URI rootUri = new URI(message.getUrl());
            String url = rootUri.resolve("blocks/").resolve(boxFile.getBlock()).toString();
            try (StorageDownload download = downloader.download(url, null)) {
                cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download.getInputStream(), targetFile.toFile(), new KeyParameter(boxFile.getKey()));
            }
        } catch (URISyntaxException e) {
            throw new QblStorageNotFound("no valid uri: " + message.getUrl());
        }
    }

    @Override
    public BoxObject loadFileMetadata(ShareNotificationMessage message, AuthenticatedDownloader downloader) throws IOException, QblStorageException, UnmodifiedException, InvalidKeyException {
        Path tmpFile = Files.createTempFile("qfm", "");
        try (StorageDownload download = downloader.download(message.getUrl(), null)) {
            new CryptoUtils().decryptFileAuthenticatedSymmetricAndValidateTag(download.getInputStream(), tmpFile.toFile(), new KeyParameter(message.getKey().getKey()));
        }
        return FileMetadata.Companion.openExisting(tmpFile.toFile()).getFile();
    }

    @Override
    public void shareAndSendMessage(Identity sender, Contact receiver, BoxFile objectToShare, String message, BoxNavigation navigation) throws QblStorageException, PersistenceException, QblNetworkInvalidResponseException {
        QblECPublicKey owner = sender.getEcPublicKey();
        BoxExternalReference ref = navigation.share(owner, objectToShare, receiver.getKeyIdentifier());
        ShareNotificationMessage share = new ShareNotificationMessage(ref.url, Hex.toHexString(ref.key), message);
        DropMessage dropMessage = new DropMessage(sender, share.toJson(), DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION);
        dropConnector.send(receiver, dropMessage);
        dropMessageRepository.addMessage(
            dropMessage,
            sender,
            receiver,
            true
        );
    }
}
