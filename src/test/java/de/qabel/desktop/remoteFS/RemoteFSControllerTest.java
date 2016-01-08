package de.qabel.desktop.remoteFS;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import javafx.scene.control.TreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RemoteFSControllerTest {

    private BoxNavigation nav;
    private LocalWriteBackend localWrite;
    private RemoteFSController controller = new RemoteFSController();
    private UUID uuid = UUID.randomUUID();
    private final String prefix = UUID.randomUUID().toString();
    private File file;
    private File localStorageFile;
    @Before
    public void setUp() throws Exception {

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
    }

    @Test
    public void showsEmptyStringOnUnknownColumn() {
        try {

            TreeItem rootNode = controller.calculateFolderStructure(nav);
            assertThat(rootNode.getChildren().size(), is(0));

        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCalculateFolderStructure() {
        try {

            nav.createFolder("folder");
            nav.commit();

            TreeItem rootNode = controller.calculateFolderStructure(nav);
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

            TreeItem rootNode = controller.calculateFolderStructure(nav);
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

            TreeItem rootNode = controller.calculateFolderStructure(nav);
            assertThat(rootNode.getChildren().size(), is(1));
            TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
            assertThat(subnode.getChildren().size(), is(1));

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

                TreeItem rootNode = controller.calculateFolderStructure(nav);
                assertThat(rootNode.getChildren().size(), is(1));
                TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
                assertThat(subnode.getChildren().size(), is(1));

            } catch (QblStorageException e) {
                e.printStackTrace();
            }

        }
}
