package de.qabel.desktop.CellValueFactory;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.remoteFS.RemoteFSApplication;
import de.qabel.desktop.storage.*;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
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


public class BoxFileCellValueFactoryTest {

    BoxNavigation nav;
    BoxVolume volume;
    CryptoUtils utils;
    QblECKeyPair keyPair;
    byte[] deviceID;
    Path tempFolder;
    ObservableValue<String> column;
    private TreeTableView treeTable;
    TreeItem<BoxObject> rootNode = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
    UUID uuid = UUID.randomUUID();
    final String bucket = "qabel";
    final String prefix = UUID.randomUUID().toString();


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

        utils = new CryptoUtils();
        deviceID = utils.getRandomBytes(16);
        keyPair = new QblECKeyPair();
        tempFolder = Files.createTempDirectory("");
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        volume = new BoxVolume(new LocalReadBackend(tempFolder),
                new LocalWriteBackend(tempFolder),
                keyPair, deviceID, new File(System.getProperty("java.io.tmpdir")));

        volume.createIndex(bucket, prefix);
        nav = volume.navigate();

        TreeItem<BoxObject> BoxObjectTreeItem = new TreeItem<>(new BoxFile("block", "File", 1L, 2L, new byte[16]));
        rootNode.getChildren().add(BoxObjectTreeItem);

    }

    @Test
    public void testEmpty() {

        treeTable = new TreeTableView(rootNode);
        column = new BoxFileCellValueFactory("").
                call(new TreeTableColumn.CellDataFeatures<>(
                        treeTable,
                        new TreeTableColumn<>(),
                        rootNode.getChildren().get(0)));

        assertThat("", is(column.getValue()));
    }

    @Test
    public void testSize() {

        treeTable = new TreeTableView(rootNode);
        column = new BoxFileCellValueFactory("size").
                call(new TreeTableColumn.CellDataFeatures<>(
                        treeTable,
                        new TreeTableColumn<>(),
                        rootNode.getChildren().get(0)));
        BoxFile subnode =  (BoxFile) rootNode.getChildren().get(0).getValue();
        assertThat(subnode.size.toString(), is(column.getValue()));
    }

    @Test
    public void testDate() {

        treeTable = new TreeTableView(rootNode);
        column = new BoxFileCellValueFactory("mtime").
                call(new TreeTableColumn.CellDataFeatures<>(
                        treeTable,
                        new TreeTableColumn<>(),
                        rootNode.getChildren().get(0)));

        BoxFile subnode =  (BoxFile) rootNode.getChildren().get(0).getValue();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String date = dateFormat.format(subnode.mtime);
        assertThat(date, is(column.getValue()));
    }
}
