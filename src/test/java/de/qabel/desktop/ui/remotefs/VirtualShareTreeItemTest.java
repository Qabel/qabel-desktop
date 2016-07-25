package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.AuthenticatedDownloader;
import de.qabel.box.storage.BoxExternalFile;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.LocalReadBackend;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.StubSharingService;
import de.qabel.desktop.config.ShareNotifications;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.ui.AbstractFxTest;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

public class VirtualShareTreeItemTest extends AbstractFxTest {
    public static final long SECOND_SHARE_SIZE = 200L;
    public static final long FIRST_SHARE_SIZE = 100L;
    private VirtualShareTreeItem item;
    private StubSharingService sharingService = new StubSharingService();
    private AuthenticatedDownloader readBackend;
    private ShareNotifications notifications = new ShareNotifications();
    private BoxObject value = new FakeBoxObject("test");
    private Node graphic = new Label("test");

    @Before
    public void setUp() throws Exception {
        readBackend = new LocalReadBackend(Files.createTempDirectory("qbl_test"));
    }

    private BoxExternalFile createShareWitzSize(long shareSize) {
        return new BoxExternalFile(
            new QblECPublicKey(new byte[0]),
            "prefix",
            "block",
            "name",
            shareSize,
            0L,
            "key".getBytes(),
            null
        );
    }

    @Test
    public void testRefreshesShares() throws Exception {
        ShareNotificationMessage message = new ShareNotificationMessage("http://testurl", "key", "message");
        sharingService.share = createShareWitzSize(FIRST_SHARE_SIZE);
        notifications.add(message);
        item = createItem();

        assertShareSize(FIRST_SHARE_SIZE);

        sharingService.share = createShareWitzSize(SECOND_SHARE_SIZE);
        item.refresh();
        assertShareSize(SECOND_SHARE_SIZE);
    }

    private void assertShareSize(long size) {
        assertAsync(() -> {
            ObservableList<TreeItem<BoxObject>> children = item.getChildren();
            assertFalse(children.isEmpty());
            BoxExternalFile object2 = (BoxExternalFile) children.get(0).getValue();
            return object2.getSize();
        }, equalTo(size));
    }

    private VirtualShareTreeItem createItem() {
        return new VirtualShareTreeItem(
            sharingService,
            readBackend,
            notifications,
            value,
            graphic
        );
    }
}
