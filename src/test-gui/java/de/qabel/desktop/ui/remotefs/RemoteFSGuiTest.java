package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.desktop.storage.BoxExternalFile;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;

import java.nio.file.Paths;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RemoteFSGuiTest extends AbstractGuiTest<RemoteFSController> {
	private final BoxFile boxFile = new BoxFile("prefix", "123", "filename", 1L, 2L, new byte[0]);
	private StubSharingService sharingService = new StubSharingService();
	private BoxNavigationStub rootNavigation;
	private RemoteBrowserPage page;

	@Override
	protected FXMLView getView() {
		rootNavigation = new BoxNavigationStub(null, Paths.get("/"));
		rootNavigation.files.add(boxFile);
		BoxVolumeStub volume = new BoxVolumeStub();
		volume.rootNavigation = rootNavigation;
		boxVolumeFactory.boxVolume = volume;
		diContainer.put("sharingService", sharingService);

		return new RemoteFSView();
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		page = new RemoteBrowserPage(baseFXRobot, robot, controller);
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
		sharingService.loadFileMetadata = new BoxExternalFile(identity.getEcPublicKey(), "prefix", "block", "share name", 123L, 123L, new byte[0]);
		clientConfiguration.getShareNotification(identity).add(notification);

		int sharedIndex = 1;
		expandNode(0);

		RemoteBrowserRow sharedRow = page.getRow(sharedIndex);
		sharedRow.downloadIcon().hover();
		sharedRow.downloadIcon().assertVisible();
		sharedRow.deleteIcon().assertVisible();

		assertThat(controller.shareRoot.getChildren().size(), is(1));
		assertThat(controller.shareRoot.getChildren().get(0).getValue().getName(), equalTo("share name"));
	}

	private FxRobot expandNode(int nodeToExpand) {
		waitUntil(() -> getNodes(".tree-disclosure-node > .arrow").size() >= nodeToExpand, 5000L);
		return clickOn(getNodes(".tree-disclosure-node > .arrow").get(nodeToExpand));
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

	private Contact addContact(String name) {
		Contacts contacts = contactRepository.findContactsFromOneIdentity(identity);
		Contact contact = new Contact(name, new LinkedList<>(), new QblECKeyPair().getPub());
		contacts.put(contact);
		return contact;
	}

	@Test
	public void unshareFile() throws Exception {
		rootNavigation.share(identity.getEcPublicKey(), boxFile, "receiver");

		page.getRow(2).share()
				.unshare()
				.close();

		StubSharingService.ShareRequest shared = sharingService.shared;
		assertNull(shared);

		page.getRow(2).shareIcon().assertHidden();
	}
}
