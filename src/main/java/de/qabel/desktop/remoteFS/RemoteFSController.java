package de.qabel.desktop.remoteFS;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.CellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;


public class RemoteFSController implements Initializable {

    private ImageView folderIcon = new ImageView(
            new Image(getClass().getResourceAsStream("/folder.png"))
    );

    @FXML
    private TreeTableView treeTable;
    @FXML
    private TreeTableColumn<BoxObject, String> nameColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> sizeColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> dateColumn;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {


        try {
            BoxNavigation nav = createSetup();
            uploadFilesAndFolders(nav);

            TreeItem rootNodeWithChilds = calculateFolderStructure(nav);
            treeTable.setRoot(rootNodeWithChilds);
        } catch (QblStorageException e) {
            e.printStackTrace();
        }
        calculateTableContent();


    }

    TreeItem calculateFolderStructure(BoxNavigation nav) throws QblStorageException {

      //  BoxFolder rootFolder = new BoxFolder("block", "root Folder", new byte[16]);

        TreeItem<BoxObject> rootNode = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
        TreeItem<BoxObject> parentNode = calculateSubFolderStructure(nav, rootNode, true);
        parentNode.setExpanded(true);
        return parentNode;
    }

    private TreeItem<BoxObject> calculateSubFolderStructure(
            BoxNavigation nav,
            TreeItem<BoxObject> TreeNode,
            boolean first) throws QblStorageException {

        BoxNavigation target;
        if (first) {
            target = nav;
        } else {
            target = nav.navigate((BoxFolder) TreeNode.getValue());
        }

        for (BoxFile file : target.listFiles()) {
            TreeItem<BoxObject> BoxObjectTreeItem = new TreeItem<>((BoxObject) file);
            TreeNode.getChildren().add(BoxObjectTreeItem);






        }
        for (BoxFolder subFolder : target.listFolders()) {

            TreeItem<BoxObject> BoxObjectTreeItem = new TreeItem<>(subFolder);

            TreeNode.getChildren().add(BoxObjectTreeItem);


            calculateSubFolderStructure(nav, BoxObjectTreeItem, false);
        }

        return TreeNode;
    }

    private void calculateTableContent() {
        nameColumn.setCellValueFactory(new BoxObjectCellValueFactory("name"));
        sizeColumn.setCellValueFactory(new BoxObjectCellValueFactory("size"));
        dateColumn.setCellValueFactory(new BoxObjectCellValueFactory("mtime"));

        treeTable.getColumns().setAll(nameColumn, sizeColumn, dateColumn);
    }

    private void uploadFilesAndFolders(BoxNavigation nav) throws QblStorageException {
        List<BoxFolder> allFolders = nav.listFolders();

        for (BoxFolder fo : allFolders) {
            nav.navigate(fo);
            nav.commit();
            nav.delete(fo);
            nav.commit();
        }

        try {
            BoxFolder folder = nav.createFolder("folder1");
            nav.commit();

            BoxFolder folder2 = nav.createFolder("folder2");
            nav.commit();

            BoxFolder folder3 = nav.createFolder("folder3");
            nav.commit();

            File file1 = File.createTempFile("File0", ".txt", new File(System.getProperty("java.io.tmpdir")));

            nav.navigate(folder).upload("File0", file1);
            nav.navigate(folder).commit();

            BoxNavigation navFolder1 = nav.navigate(folder);
            navFolder1.upload("File1", file1);
            navFolder1.commit();

            BoxNavigation navFolder2 = nav.navigate(folder2);
            navFolder2.upload("File2", file1);
            navFolder2.commit();

            BoxNavigation navFolder3 = nav.navigate(folder3);
            BoxFolder folder4 = navFolder3.createFolder("folder4");
            navFolder3.commit();
            navFolder3.upload("File3", file1);
            navFolder3.commit();

            BoxNavigation navFolder4 = nav.navigate(folder4);
            navFolder4.upload("File4", file1);
            navFolder4.commit();

            File file2 = File.createTempFile("File2", ".txt", new File(System.getProperty("java.io.tmpdir")));
            nav.upload("File0", file2);
            nav.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BoxNavigation createSetup() throws QblStorageException {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        QblECKeyPair keyPair = new QblECKeyPair();
        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();
        final String bucket = "qabel";
        final String prefix = "qabelTest";

        BoxVolume volume = new BoxVolume(bucket, prefix, chain.getCredentials(), keyPair, deviceID,
                new File(System.getProperty("java.io.tmpdir")));

        volume.createIndex(bucket, prefix);
        return volume.navigate();
    }
}