package de.qabel.desktop.ui.remotefs;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RemoteFSControllerTest extends AbstractControllerTest {

    private BoxNavigation nav;
    private LocalWriteBackend localWrite;
    private RemoteFSController controller = new RemoteFSController();
    private UUID uuid = UUID.randomUUID();
    private final String prefix = UUID.randomUUID().toString();
    private File file;
    private File localStorageFile;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        file = File.createTempFile("File2", ".txt", new File(System.getProperty("java.io.tmpdir")));
        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        QblECKeyPair keyPair = new QblECKeyPair();
        Path tempFolder = Files.createTempDirectory("");

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        LocalReadBackend localRead = new LocalReadBackend(tempFolder);
        localWrite = new LocalWriteBackend(tempFolder);
        localStorageFile = new File(System.getProperty("java.io.tmpdir"));

        BoxVolume volume = new BoxVolume(
                localRead,
                localWrite,
                keyPair,
                deviceID,
                localStorageFile,
                prefix);

        String bucket = "qabel";
        volume.createIndex(bucket, prefix);
        nav = volume.navigate();
    }

    @After
    public void after() throws Exception {
        localWrite.delete(localStorageFile.getName());
        File dir = new File("tmp/testFolder");
        FileUtils.deleteDirectory(dir);

    }

    @Test
    public void showsEmptyStringOnUnknownColumn() {
        TreeItem rootNode = getRoot();
        assertThat(rootNode.getChildren().size(), is(0));
    }

    private LazyBoxFolderTreeItem getRoot() {
        LazyBoxFolderTreeItem rootNode = new LazyBoxFolderTreeItem(new BoxFolder("", "root", new byte[0]), nav);
        loadChildren(rootNode);
        return rootNode;
    }

    private ObservableList loadChildren(LazyBoxFolderTreeItem node) {
        ObservableList children = node.getChildren();
        while (node.isLoading()) {
            Thread.yield();
        }
        return children;
    }

    @Test
    public void testCalculateFolderStructure() {
        try {

            nav.createFolder("folder");
            nav.commit();

            TreeItem rootNode = getRoot();
            assertThat(rootNode.getChildren().size(), is(1));

        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCalculateFileStructure() {
        try {

            nav.upload("File", file);
            nav.commit();

            TreeItem rootNode = getRoot();
            assertThat(rootNode.getChildren().size(), is(1));

        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserveFilesSubfolder() {
        try {
            BoxFolder folder = nav.createFolder("folder");
            BoxNavigation navFolder1 = nav.navigate(folder);
            navFolder1.upload("File", file);
            navFolder1.commit();

            TreeItem rootNode = getRoot();
            assertThat(rootNode.getChildren().size(), is(1));
            TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
            assertThat(loadChildren((LazyBoxFolderTreeItem) subnode).size(), is(1));

        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserveFoldersSubfolder() {
        try {

            BoxFolder folder = nav.createFolder("folder");
            nav.commit();

            BoxNavigation navFolder3 = nav.navigate(folder);
            navFolder3.createFolder("subFolder");
            navFolder3.commit();

            TreeItem rootNode = getRoot();
            assertThat(rootNode.getChildren().size(), is(1));
            TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
            assertThat(loadChildren((LazyBoxFolderTreeItem) subnode).size(), is(1));

        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUploadedDirectoryInRootNode() throws QblStorageException, IOException {

        File dir = craeteFileAndFolder();

        controller.nav = nav;
        controller.uploadedDirectory(dir, null);
        assertThat(controller.nav.listFolders().size(), is(1));
        assertThat(controller.nav.listFiles().size(), is(0));

        BoxFolder folder = controller.nav.listFolders().get(0);
        BoxNavigation newNav = nav.navigate(folder);

        assertThat(newNav.listFiles().size(), is(1));
        assertThat(newNav.listFolders().size(), is(1));
    }

    @Test
    public void testUploadedDirectoryInChildNode() throws QblStorageException, IOException {

        File dir = new File("tmp/testFolder");
        dir.mkdirs();
        BoxFolder folder = createBoxFolder(dir);

        File subdir = new File(dir, "subFolder");
        subdir.mkdirs();
        controller.uploadedDirectory(dir, folder);

        BoxNavigation newNav = nav.navigate(folder);
        assertThat(newNav.listFiles().size(), is(0));
        assertThat(newNav.listFolders().size(), is(1));

    }

    @Test
    public void testCreateFolder() throws QblStorageException {

        File dir = new File("tmp/testFolder");
        dir.mkdirs();
        BoxFolder folder = createBoxFolder(dir);
        controller.createFolder("NewFolder", folder);
        BoxNavigation newNav = nav.navigate(folder);
        assertThat(newNav.listFiles().size(), is(0));
        assertThat(newNav.listFolders().size(), is(1));

    }


    @Test
    public void TestUploadFiles() throws QblStorageException, IOException {
        File dir = new File("tmp/testFolder");
        dir.mkdirs();
        BoxFolder folder = createBoxFolder(dir);
        File tmp = new File(dir, "tmp1.txt");
        tmp.createNewFile();
        controller.uploadFiles(tmp, folder);
        BoxNavigation newNav = nav.navigate(folder);
        assertThat(newNav.listFiles().size(), is(1));
        assertThat(newNav.listFolders().size(), is(0));

    }

    @Test
    public void TestDontDeleteBoxFolder() throws QblStorageException, IOException {
        File dir = craeteFileAndFolder();
        controller.nav = nav;
        controller.uploadedDirectory(dir, null);
        BoxFolder folder = controller.nav.listFolders().get(0);
        controller.deleteBoxObject(1, folder, null);
        assertThat(controller.nav.listFolders().size(), is(1));
        assertThat(controller.nav.listFiles().size(), is(0));
    }

    @Test
    public void TestDeleteBoxFolder() throws QblStorageException, IOException {
        File dir = craeteFileAndFolder();
        controller.nav = nav;
        controller.uploadedDirectory(dir, null);

        BoxFolder folder = controller.nav.listFolders().get(0);
        controller.deleteBoxObject(0, folder, null);
        assertThat(controller.nav.listFolders().size(), is(0));
        assertThat(controller.nav.listFiles().size(), is(0));
    }

    @Test
    public void TestDeleteBoxFile() throws QblStorageException, IOException {
        File dir = craeteFileAndFolder();
        controller.nav = nav;
        controller.uploadedDirectory(dir, null);

        BoxFolder folder = nav.listFolders().get(0);

        BoxNavigation newNav = nav.navigate(folder);
        BoxObject boxFile = newNav.listFiles().get(0);
        controller.deleteBoxObject(0, boxFile, folder);

        newNav = nav.navigate(folder);
        assertThat(newNav.listFiles().size(), is(0));
    }

    @Test
    public void TestDeleteBoxFileFromRootNode() throws QblStorageException, IOException {
        File dir = craeteFileAndFolder();
        controller.nav = nav;
        controller.uploadedDirectory(dir, null);
        BoxFolder folder = nav.listFolders().get(0);
        controller.deleteBoxObject(0, folder, null);
        assertThat(nav.listFiles().size(), is(0));
    }

    private BoxFolder createBoxFolder(File dir) throws QblStorageException {
        controller.nav = nav;
        controller.uploadedDirectory(dir, null);
        return controller.nav.listFolders().get(0);
    }

    private File craeteFileAndFolder() throws IOException {
        File dir = new File("tmp/testFolder");
        dir.mkdirs();
        File tmp = new File(dir, "tmp1.txt");
        tmp.createNewFile();
        File subdir = new File(dir, "subFolder");
        subdir.mkdirs();
        return dir;
    }
}
