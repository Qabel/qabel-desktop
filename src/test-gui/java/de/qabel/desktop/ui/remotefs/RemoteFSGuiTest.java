package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.desktop.storage.BoxExternalFile;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.FileMetadata;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.event.EventType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;

import java.nio.file.Paths;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RemoteFSGuiTest extends AbstractGuiTest<RemoteFSController> {
	private final BoxFile boxFile = new BoxFile("123", "filename", 1L, 2L, new byte[0]);
	private StubSharingService sharingService = new StubSharingService();
	private BoxNavigationStub rootNavigation;

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
	}

	@Test
	public void optionsOnHover() throws InterruptedException {
		int rootIndex = 1;
		waitUntil(() -> !getFirstNode("#download_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#delete_" + rootIndex).isVisible());

		moveTo("#download_" + rootIndex);

		waitUntil(() -> getFirstNode("#download_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#delete_" + rootIndex).isVisible());

		robot.moveTo(stage);

		waitUntil(() -> !getFirstNode("#download_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#delete_" + rootIndex).isVisible());
	}

	@Test
	public void loadsShares() throws Exception {
		ShareNotificationMessage notification = new ShareNotificationMessage("http://some.url.com", "key", "message");
		sharingService.loadFileMetadata = new BoxExternalFile(identity.getEcPublicKey(), "block", "share name", 123L, 123L, new byte[0]);
		clientConfiguration.getShareNotification(identity).add(notification);

		int sharedIndex = 1;
		expandNode(0);

		moveTo(getFirstNode("#download_" + sharedIndex));
		assertTrue(getFirstNode("#download_" + sharedIndex).isVisible());
		assertTrue(getFirstNode("#delete_" + sharedIndex).isVisible());

		assertThat(controller.shareRoot.getChildren().size(), is(1));
		assertThat(controller.shareRoot.getChildren().get(0).getValue().getName(), equalTo("share name"));
	}

	private FxRobot expandNode(int nodeToExpand) {
		waitUntil(() -> getNodes(".tree-disclosure-node > .arrow").size() >= nodeToExpand, 5000L);
		return clickOn(getNodes(".tree-disclosure-node > .arrow").get(nodeToExpand));
	}

	@Test
	public void shareFile() throws Exception {
		Contacts contacts = contactRepository.findContactsFromOneIdentity(identity);
		Contact otto = new Contact("Otto", new LinkedList<>(), new QblECPublicKey(new byte[0]));
		contacts.put(otto);

		waitUntil(() -> getNodes(".cell").size() > 2);
		clickOn(getFirstNode("#share_2"));
		waitForNode(".detailsContainer");
		waitUntil(() -> getFirstNode(".detailsContainer").isVisible(), 5000L);

		clickOn(getFirstNode("#shareReceiver")).write("tto").push(KeyCode.ENTER);

		waitUntil(() -> controller.fileDetails.dialog != null);
		runLaterAndWait(() -> controller.fileDetails.dialog.getEditor().setText("this is a sharing message"));
		clickOn(controller.fileDetails.dialog.getDialogPane().lookupButton(ButtonType.OK));

		StubSharingService.ShareRequest shared = sharingService.shared;
		assertNotNull(shared);
		assertEquals("this is a sharing message", shared.message);
		assertSame(otto, shared.receiver);
		assertSame(identity, shared.sender);
		assertSame(boxFile, shared.objectToShare);
		assertSame(rootNavigation, shared.navigation);
	}
}
