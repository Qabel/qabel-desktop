package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractFxTest;
import de.qabel.desktop.util.UTF8Converter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class ShareNotificationRendererTest extends AbstractFxTest {
    private ResourceBundle resourceBundle;
    private StubSharingService sharingService = new StubSharingService();
    private ShareNotificationRenderer renderer = new ShareNotificationRenderer(null, sharingService);
    private ShareNotificationMessage share;

    @Before
    public void setUp() throws Exception {
        resourceBundle = ResourceBundle.getBundle("ui", new Locale("te", "ST"), new UTF8Converter());
        share = new ShareNotificationMessage("shareUrl", "shareKey", "shareMessage");
    }

    @Test
    public void rendersString() {
        String message = renderer.renderString(share.toJson(), resourceBundle);

        assertEquals("sharedFile\n(\"shareMessage\")", message);
    }

    @Test
    public void renderStringHandlesOutdatedShares() {
        sharingService.storageException = new QblStorageNotFound("file not found");
        String message = renderer.renderString(share.toJson(), resourceBundle);

        assertEquals("sharedFileNoLongerAvailableTranslation\n(\"shareMessage\")", message);
    }

    @Test
    public void renderStringHandlesOtherMetadataLoadingExceptions() {
        sharingService.ioException = new IOException("something failed...");
        String message = renderer.renderString(share.toJson(), resourceBundle);

        assertEquals("remoteFileFailedToFetchShareMetadataTranslation\n(\"shareMessage\")", message);
    }

    private class StubSharingService implements SharingService {
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
}
