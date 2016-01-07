package de.qabel.desktop.remoteFS;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import javafx.scene.control.TreeItem;
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


public class RemoteFSControllerTest {

    private BoxNavigation nav;
    private BoxVolume volume;
    private CryptoUtils utils;
    private QblECKeyPair keyPair;
    private byte[] deviceID;
    private Path tempFolder;
    private LocalReadBackend localRead;
    private LocalWriteBackend localWrite;
    private RemoteFSController controller = new RemoteFSController();
    private UUID uuid = UUID.randomUUID();
    private final String bucket = "qabel";
    private final String prefix = UUID.randomUUID().toString();
    private final String testFileName = "src/test/java/de/qabel/desktop/remoteFS/testFile.txt";
    private File file;
    private File localStorageFile;
    @Before
    public void setUp() throws Exception {

        file = File.createTempFile("File2", ".txt", new File(System.getProperty("java.io.tmpdir")));
        utils = new CryptoUtils();
        deviceID = utils.getRandomBytes(16);
        keyPair = new QblECKeyPair();
        tempFolder = Files.createTempDirectory("");

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        localRead = new LocalReadBackend(tempFolder);
        localWrite = new LocalWriteBackend(tempFolder);
        localStorageFile = new File(System.getProperty("java.io.tmpdir"));

        volume = new BoxVolume(
                localRead,
                localWrite,
                keyPair,
                deviceID,
                localStorageFile);

        volume.createIndex(bucket, prefix);
        nav = volume.navigate();
    }

    @After
    public void after() throws Exception {

        localWrite.delete(localStorageFile.getName());
    }

    @Test
    public void testEmpty() {
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
