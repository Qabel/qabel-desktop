package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxObject;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.TreeItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static org.junit.Assert.*;

public class FilterableFolderTreeItemTest extends AbstractGuiTest<RemoteFSController> {
    private FilterableFolderTreeItem folderTree;
    private BoxFile subFile;
    private BoxFile subSubFile;
    private BoxFolder subFolder;
    private BoxNavigationStub navigation;

    @Override
    protected FXMLView getView() {
        subFile = new BoxFile("prefix", "block", "fileName", 0L, 0L, new byte[0]);
        subSubFile = new BoxFile("prefix", "block", "innerFileName", 0L, 0L, new byte[0]);
        subFolder = new BoxFolder("prefix", "folderName", new byte[0]);

        BoxFolder folder = new BoxFolder("ref", "folder", new byte[0]);
        BoxNavigationStub indexNav = new BoxNavigationStub(null, Paths.get("/"));
        navigation = new BoxNavigationStub(indexNav, Paths.get("/folder"));
        navigation.files.add(subFile);
        navigation.folders.add(subFolder);
        try {
            navigation.navigate("folderName").files.add(subSubFile);
        } catch (QblStorageException e) {
            fail(e.getMessage());
        }

        ((BoxVolumeStub)boxVolumeFactory.boxVolume).rootNavigation = navigation;

        return new RemoteFSView();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folderTree = controller.rootItem;
        runLaterAndWait(folderTree::getChildren);
        expectChildren(2);
        waitForUI();
    }

    @Test
    public void showsOriginalChildrenWithoutFilter() throws Exception {
        expectChildren(2);
        assertEquals("folderName", folderTree.getChildren().get(0).getValue().getName());
        assertEquals("fileName", folderTree.getChildren().get(1).getValue().getName());
    }

    @Test
    public void hidesInvalidChildren() throws Exception {
        search("notTheName");
        expectChildren(0);
    }

    @Ignore(value = "the test is bad and the feature will be removed soon (replaced with a list)")
    @Test
    public void showsFilterMatches() throws Exception {
        Thread.sleep(500);  // sorry, couldn't handle this otherwise
        search("inner");
        expectChildren(1);
        TreeItem<BoxObject> firstItem = folderTree.getChildren().get(0);
        assertSame(subFolder, firstItem.getValue());
        assertTrue(firstItem.isExpanded());
        assertEquals(1, firstItem.getChildren().size());
    }

    private void expectChildren(int count) {
        waitForUI();
        waitUntil(() -> folderTree.getChildren().size() == count, 10000L, () ->
            "expected " + count + " children but got " + folderTree.getChildren()
        );
        waitForUI();
    }

    private void waitForUI() {
        baseFXRobot.waitForIdle();
    }

    @Test
    public void reactsOnRemoteChanges() throws Exception {
        navigation.navigate("folderName").files.remove(subSubFile);
        navigation.navigate("folderName").pushNotification(subSubFile, DELETE);


        waitForUI();
        waitUntil(() -> folderTree.getChildren().get(0).getChildren().isEmpty(), 10000000L);

        search("inner");
        expectChildren(0);
    }

    private void search(String query) throws InterruptedException {
        waitForUI();
        if (controller.searchQuery.textProperty().isNotEmpty().get()) {
            runLaterAndWait(() -> controller.searchQuery.textProperty().set(""));
        }
        clickOn("#searchQuery").write(query);
    }
}
