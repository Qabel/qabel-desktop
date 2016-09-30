package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.box.storage.BoxExternalFile;
import de.qabel.box.storage.BoxFile;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.stub;

public class RemoteFSGuiTest extends AbstractGuiTest<RemoteFSController> {
    private final BoxFile boxFile = new BoxFile("prefix", "123", "filename", 1L, 2L, new byte[0]);
    private StubSharingService sharingService = new StubSharingService();
    private BoxNavigationStub rootNavigation;
    private RemoteBrowserPage page;
    private Contacts contacts;

    @Override
    protected FXMLView getView() {
        rootNavigation = BoxNavigationStub.create();
        rootNavigation.files.add(boxFile);
        BoxVolumeStub volume = new BoxVolumeStub();
        volume.rootNavigation = rootNavigation;
        stub(boxVolumeFactory.getVolume(any(), any())).toReturn(volume);
        diContainer.put("sharingService", sharingService);

        return new RemoteFSView();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = new RemoteBrowserPage(baseFXRobot, robot, controller);
        contacts = contactRepository.find(identity);
    }

    @Test
    public void optionsOnHover() throws InterruptedException {
        int rootIndex = 1;
        robot.moveTo(stage);

        RemoteBrowserRow row = page.getRow(rootIndex);
        row.downloadIcon().assertHidden();
        row.uploadFileIcon().assertHidden();
        row.uploadFolderIcon().assertHidden();
        row.createFolderIcon().assertHidden();
        row.deleteIcon().assertHidden();

        row.downloadIcon().hover();

        row.downloadIcon().assertVisible();
        row.uploadFileIcon().assertVisible();
        row.uploadFolderIcon().assertVisible();
        row.createFolderIcon().assertVisible();
        row.deleteIcon().assertVisible();

        robot.moveTo(stage);

        row.downloadIcon().assertHidden();
        row.uploadFileIcon().assertHidden();
        row.uploadFolderIcon().assertHidden();
        row.createFolderIcon().assertHidden();
        row.deleteIcon().assertHidden();
    }

    @Test
    public void loadsShares() throws Exception {
        ShareNotificationMessage notification = new ShareNotificationMessage("http://some.url.com", "key", "message");
        sharingService.loadFileMetadata = new BoxExternalFile(identity.getEcPublicKey(), "prefix", "block", "share name", 123L, 123L, new byte[0], null);
        shareNotificationRepository.save(identity, notification);

        int sharedIndex = 1;
        page.expandNode(0);

        RemoteBrowserRow sharedRow = page.getRow(sharedIndex);
        sharedRow.downloadIcon().hover();
        sharedRow.downloadIcon().assertVisible();
        sharedRow.deleteIcon().assertVisible();

        assertThat(controller.shareRoot.getChildren().size(), is(1));
        assertThat(controller.shareRoot.getChildren().get(0).getValue().getName(), equalTo("share name"));
    }

    @Test
    public void shareFile() throws Exception {
        Contact otto = addContact("Otto");

        page.getRow(2).share()
            .shareBySearch("tto", "this is a sharing message")
            .close();

        StubSharingService.ShareRequest shared = sharingService.shared;
        assertNotNull(shared);
        assertEquals("this is a sharing message", shared.message);
        assertSame(otto, shared.receiver);
        assertSame(identity, shared.sender);
        assertSame(boxFile, shared.objectToShare);
        assertSame(rootNavigation, shared.navigation);


        page.getRow(2).shareIcon().assertVisible();
    }

    @Test(timeout = 20000L)
    public void doubleShare() throws Exception {
        addContact("Otto");
        addContact("Bob");

        page.getRow(2).share()
            .shareFirst("this is a sharing message")
            .shareFirst("this is another sharing message")
            .assertReceivers(2);
    }

    private Contact addContact(String name) throws PersistenceException {
        Contact contact = new Contact(name, new LinkedList<>(), new QblECKeyPair().getPub());
        contacts.put(contact);
        contactRepository.save(contact, identity);
        return contact;
    }

    @Test
    @Ignore(value = "unignore when jenkins can handle GUI actions")
    public void unshareFile() throws Exception {
        rootNavigation.share(identity.getEcPublicKey(), boxFile, "receiver");

        RemoteBrowserRow row = page.getRow(2);
        RemoteFileDetailsPage share = row.share();
        share.unshare();
        share.close();

        StubSharingService.ShareRequest shared = sharingService.shared;
        assertNull(shared);

        page.getRow(2).shareIcon().assertHidden();
    }
}
