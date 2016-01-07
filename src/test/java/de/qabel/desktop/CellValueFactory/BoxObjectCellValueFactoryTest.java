package de.qabel.desktop.CellValueFactory;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.storage.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class BoxObjectCellValueFactoryTest {

    private ObservableValue<String> column;
    private TreeTableView treeTableFile;
    private TreeTableView treeTableFolder;
    private TreeItem<BoxObject> rootNodeFolder = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
    private TreeItem<BoxObject> rootNodeFile = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
    private UUID uuid = UUID.randomUUID();
    private final String prefix = UUID.randomUUID().toString();


    @BeforeClass
    public static void setUpClass() throws InterruptedException {
        Thread t = new Thread("JavaFX Init Thread") {
            public void run() {
                Application.launch(TestApplication.class, new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
        Thread.sleep(500);
    }

    @Before
    public void setUp() throws Exception {

        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        QblECKeyPair keyPair = new QblECKeyPair();
        Path tempFolder = Files.createTempDirectory("");
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        BoxVolume volume = new BoxVolume(
                new LocalReadBackend(tempFolder),
                new LocalWriteBackend(tempFolder),
                keyPair,
                deviceID,
                new File(System.getProperty("java.io.tmpdir")));

        String bucket = "qabel";
        volume.createIndex(bucket, prefix);

        TreeItem<BoxObject> boxFolderTreeItem = new TreeItem<>(new BoxFolder("ref", "Folder", new byte[16]));
        rootNodeFolder.getChildren().add(boxFolderTreeItem);
        treeTableFolder = new TreeTableView(rootNodeFolder);

        TreeItem<BoxObject> boxFileTreeItem = new TreeItem<>(new BoxFile("block", "File", 1L, 2L, new byte[16]));
        rootNodeFile.getChildren().add(boxFileTreeItem);
        treeTableFile = new TreeTableView(rootNodeFile);

    }

    @Test
    public void testEmpty() {

        column = getColumnValue("", rootNodeFile);
        assertThat(column.getValue(), is(""));
    }

    @Test
    public void testSize() {

        column = getColumnValue("size", rootNodeFile);
        BoxFile subnode = (BoxFile) rootNodeFile.getChildren().get(0).getValue();
        assertThat(column.getValue(), is(subnode.size.toString()));
    }

    @Test
    public void testNameFile() {

        column = getColumnValue("name", rootNodeFile);

        BoxFile subnode = (BoxFile) rootNodeFile.getChildren().get(0).getValue();
        assertThat(column.getValue(), is(subnode.name));
    }

    @Test
    public void testNameFolder() {

        column = getColumnValue("name", rootNodeFolder);

        BoxFolder subnode = (BoxFolder) rootNodeFolder.getChildren().get(0).getValue();
        assertThat(column.getValue(), is(subnode.name));
    }

    @Test
    public void testDate() {

        column = getColumnValue("mtime", rootNodeFile);
        BoxFile subnode = (BoxFile) rootNodeFile.getChildren().get(0).getValue();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String date = dateFormat.format(subnode.mtime);
        assertThat(column.getValue(), is(date));
    }

    private ObservableValue<String> getColumnValue(String searchString, TreeItem<BoxObject> rootNode) {
        return new BoxObjectCellValueFactory(searchString).
                call(new TreeTableColumn.CellDataFeatures<>(
                        treeTableFile,
                        new TreeTableColumn<>(),
                        rootNode.getChildren().get(0)));
    }
}
