package de.qabel.desktop.ui.remotefs;

import com.sun.javafx.robot.impl.BaseFXRobot;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxObject;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;

import java.nio.file.Paths;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static org.junit.Assert.*;

public class FilterableFolderTreeItemTest extends AbstractControllerTest {
	private FilterableFolderTreeItem folderTree;
	private StringProperty filter;
	private BoxFile subFile;
	private BoxFile subSubFile;
	private BoxFolder subFolder;
	private BoxNavigationStub navigation;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		subFile = new BoxFile("prefix", "block", "fileName", 0L, 0L, new byte[0]);
		subSubFile = new BoxFile("prefix", "block", "innerFileName", 0L, 0L, new byte[0]);
		subFolder = new BoxFolder("prefix", "folderName", new byte[0]);

		BoxFolder folder = new BoxFolder("ref", "folder", new byte[0]);
		BoxNavigationStub indexNav = new BoxNavigationStub(null, Paths.get("/"));
		navigation = new BoxNavigationStub(indexNav, Paths.get("/folder"));
		navigation.files.add(subFile);
		navigation.folders.add(subFolder);
		navigation.navigate("folderName").files.add(subSubFile);

		folderTree = new FilterableFolderTreeItem(folder, navigation);
		filter = folderTree.filterProperty();
		expectChildren(2);
		runLaterAndWait(() -> {});
	}

	@Test
	public void showsOriginalChildrenWithoutFilter() throws Exception {
		expectChildren(2);
		assertEquals("folderName", folderTree.getChildren().get(0).getValue().getName());
		assertEquals("fileName", folderTree.getChildren().get(1).getValue().getName());

	}

	@Test
	public void hidesInvalidChildren() throws Exception {
		filter.setValue("notTheName");
		expectChildren(0);
	}

	@Test
	public void showsFilterMatches() throws Exception {
		filter.setValue("inner");
		expectChildren(1);
		TreeItem<BoxObject> firstItem = folderTree.getChildren().get(0);
		assertSame(subFolder, firstItem.getValue());
		assertTrue(firstItem.isExpanded());
		assertEquals(1, firstItem.getChildren().size());
	}

	private void expectChildren(int count) {
		runLaterAndWait(() -> {});
		waitUntil(() -> folderTree.getChildren().size() == count, 5000L, () -> "expected " + count + " children but got " + folderTree.getChildren());
	}

	@Test
	public void reactsOnRemoteChanges() throws Exception {
		filter.setValue("inner");
		expectChildren(1);
		filter.setValue("");
		expectChildren(2);

		navigation.navigate("folderName").files.remove(subSubFile);
		navigation.navigate("folderName").pushNotification(subSubFile, DELETE);

		filter.setValue("inner");
		expectChildren(0);
	}
}
