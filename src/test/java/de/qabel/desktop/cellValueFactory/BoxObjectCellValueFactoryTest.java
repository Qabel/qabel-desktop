package de.qabel.desktop.cellValueFactory;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class BoxObjectCellValueFactoryTest extends AbstractControllerTest {

    private ObservableValue<String> column;
    private TreeTableView treeTableFile;
    private TreeTableView treeTableFolder;
    private TreeItem<BoxObject> rootNodeFolder = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
    private TreeItem<BoxObject> rootNodeFile = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
    private UUID uuid = UUID.randomUUID();
    private final String prefix = UUID.randomUUID().toString();
    private Path tempFolder;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        QblECKeyPair keyPair = new QblECKeyPair();
        tempFolder = Files.createTempDirectory("");
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        BoxVolume volume = new BoxVolume(
                new LocalReadBackend(tempFolder),
                new LocalWriteBackend(tempFolder),
                keyPair,
                deviceID,
                new File(System.getProperty("java.io.tmpdir")),
                prefix
        );

        String bucket = "qabel";
        volume.createIndex(bucket, prefix);

        TreeItem<BoxObject> boxFolderTreeItem = new TreeItem<>(new BoxFolder("ref", "Folder", new byte[16]));
        rootNodeFolder.getChildren().add(boxFolderTreeItem);
        treeTableFolder = new TreeTableView(rootNodeFolder);

        TreeItem<BoxObject> boxFileTreeItem = new TreeItem<>(new BoxFile("prefix", "block", "File", 1L, 2L, new byte[16]));
        rootNodeFile.getChildren().add(boxFileTreeItem);
        treeTableFile = new TreeTableView(rootNodeFile);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(tempFolder.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.tearDown();
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
        assertThat(column.getValue(), is(subnode.getSize().toString()));
    }

    @Test
    public void testNameFile() {

        column = getColumnValue("name", rootNodeFile);

        BoxFile subnode = (BoxFile) rootNodeFile.getChildren().get(0).getValue();
        assertThat(column.getValue(), is(subnode.getName()));
    }

    @Test
    public void testNameFolder() {

        column = getColumnValue("name", rootNodeFolder);

        BoxFolder subnode = (BoxFolder) rootNodeFolder.getChildren().get(0).getValue();
        assertThat(column.getValue(), is(subnode.getName()));
    }

    @Test
    public void testDate() {

        column = getColumnValue("mtime", rootNodeFile);
        BoxFile subnode = (BoxFile) rootNodeFile.getChildren().get(0).getValue();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String date = dateFormat.format(subnode.getMtime());
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
