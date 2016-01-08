package de.qabel.desktop.remoteFS;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class RemoteFSController implements Initializable {

    private ImageView folderIcon = new ImageView(
            new Image(getClass().getResourceAsStream("/folder.png"))
    );

    final String bucket = "qabel";
    final String prefix = "qabelTest";
    private BoxVolume volume;
    private List<BoxFolder> folders;
    public static final String PRIVATE_KEY = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a";

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

            //DELETEME
           // uploadFilesAndFolders(nav);

            BoxNavigation nav = createSetup();
            TreeItem rootNodeWithChilds = calculateFolderStructure(nav);
            treeTable.setRoot(rootNodeWithChilds);
        } catch (QblStorageException e) {
            e.printStackTrace();
        }
        calculateTableContent();


    }

    TreeItem calculateFolderStructure(BoxNavigation nav) throws QblStorageException {

        TreeItem<BoxObject> rootNode = new TreeItem<>(new BoxFolder("block", "root Folder", new byte[16]));
        TreeItem<BoxObject> parentNode = calculateSubFolderStructure(nav, rootNode, true);
        parentNode.setExpanded(true);


        return parentNode;
    }

    private TreeItem<BoxObject> calculateSubFolderStructure(
            BoxNavigation nav,
            TreeItem<BoxObject> treeNode,
            boolean first) throws QblStorageException {

        BoxNavigation target;
        if (first) {
            target = nav;
        } else {
            target = nav.navigate((BoxFolder) treeNode.getValue());
        }

        for (BoxFile file : target.listFiles()) {
            TreeItem<BoxObject> BoxObjectTreeItem = new TreeItem<>((BoxObject) file);
            treeNode.getChildren().add(BoxObjectTreeItem);






        }
        for (BoxFolder subFolder : target.listFolders()) {

            TreeItem<BoxObject> BoxObjectTreeItem = new TreeItem<>(subFolder);
            treeNode.getChildren().add(BoxObjectTreeItem);


            calculateSubFolderStructure(nav, BoxObjectTreeItem, false);
        }

        return treeNode;
    }

    private void calculateTableContent() {
        nameColumn.setCellValueFactory(new BoxObjectCellValueFactory("name"));
        sizeColumn.setCellValueFactory(new BoxObjectCellValueFactory("size"));
        dateColumn.setCellValueFactory(new BoxObjectCellValueFactory("mtime"));

        treeTable.getColumns().setAll(nameColumn, sizeColumn, dateColumn);

    }

    private void uploadFilesAndFolders(BoxNavigation nav) throws QblStorageException {

        try {

            File file1 = File.createTempFile("File1", ".txt");
            for (int i = 0; i < 10; i++) {
                BoxFolder folder = nav.createFolder("folder" + i);
                nav.commit();
                for (int j = 0; j < 10; j++) {
                    BoxNavigation navFolder = nav.navigate(folder);
                    BoxFolder subFolder = navFolder.createFolder("subFolder" + i + j);
                    navFolder.commit();
                    for (int k = 0; k < 10; k++) {
                        BoxNavigation navSubFolder = nav.navigate(subFolder);
                        navSubFolder.upload("File" + i + j + k, file1);
                        navSubFolder.commit();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BoxNavigation createSetup() throws QblStorageException {

        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

        QblECKeyPair testKey = new QblECKeyPair(Hex.decode(PRIVATE_KEY));

        this.volume = new BoxVolume(bucket, prefix, chain.getCredentials(), testKey, deviceID,
                new File(System.getProperty("java.io.tmpdir")));

       // cleanVolume();
       // volume.createIndex(bucket, prefix);

        return volume.navigate();
    }

    protected void cleanVolume() {
        AmazonS3Client client = ((S3WriteBackend) volume.writeBackend).s3Client;
        ObjectListing listing = client.listObjects(bucket, prefix);
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for (S3ObjectSummary summary : listing.getObjectSummaries()) {
            keys.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
        }
        if (keys.isEmpty()) {
            return;
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
        deleteObjectsRequest.setKeys(keys);
        client.deleteObjects(deleteObjectsRequest);
    }
}