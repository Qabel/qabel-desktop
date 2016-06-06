package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.desktop.StubSharingService;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.ui.AbstractFxTest;
import de.qabel.desktop.util.UTF8Converter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

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
}
