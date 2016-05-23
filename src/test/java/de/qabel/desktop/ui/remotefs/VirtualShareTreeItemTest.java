package de.qabel.desktop.ui.remotefs;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.StubSharingService;
import de.qabel.desktop.config.ShareNotifications;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.storage.*;
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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class VirtualShareTreeItemTest extends AbstractFxTest {
    private VirtualShareTreeItem item;
    private StubSharingService sharingService = new StubSharingService();
    private AuthenticatedDownloader readBackend;
    private ShareNotifications notifications = new ShareNotifications();
    private BoxObject value = new FakeBoxObject("test");
    private Node graphic = new Label("test");
    private BoxExternalFile share;
    private BoxExternalFile share2;

    @Before
    public void setUp() throws Exception {
        readBackend = new LocalReadBackend(Files.createTempDirectory("qbl_test"));
        share = new BoxExternalFile(
            new QblECPublicKey(new byte[0]),
            "prefix",
            "block",
            "name",
            100L,
            123L,
            new byte[0]
        );
        share2 = new BoxExternalFile(
            new QblECPublicKey(new byte[0]),
            "prefix",
            "block",
            "name",
            200L,
            123L,
            new byte[0]
        );
    }

    @Test
    public void testRefreshesShares() throws Exception {
        ShareNotificationMessage message = new ShareNotificationMessage("http://testurl", "key", "message");
        sharingService.share = share;
        notifications.add(message);
        item = createItem();

        waitUntil(() -> !item.getChildren().isEmpty());
        BoxExternalFile object1 = (BoxExternalFile)item.getChildren().get(0).getValue();
        assertThat(object1.getSize(), equalTo(100L));

        sharingService.share = share2;
        item.refresh();
        assertAsync(() -> {
            ObservableList<TreeItem<BoxObject>> children = item.getChildren();
            assertFalse(children.isEmpty());
            BoxExternalFile object2 = (BoxExternalFile) children.get(0).getValue();
            return object2.getSize();
        }, equalTo(200L));
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
