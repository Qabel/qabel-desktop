package de.qabel.desktop.ui.remotefs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.spongycastle.util.encoders.Hex;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class RemoteFSController extends AbstractController implements Initializable {

    private static final String PRIVATE_KEY = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a";
    public static final String ROOT_FOLDER_NAME = "root Folder";
    private final String bucket = "qabel";
    private final String prefix = "qabelTest";
    private BoxVolume volume;
    BoxNavigation nav;

    TreeItem<BoxObject> selectedFolder;
    @FXML
    private TreeTableView<BoxObject> treeTable;
    @FXML
    private TreeTableColumn<BoxObject, String> nameColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> sizeColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> dateColumn;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        try {
            nav = createSetup();

            LazyBoxFolderTreeItem rootItem = new LazyBoxFolderTreeItem(new BoxFolder("block", "root Folder", new byte[16]), nav);
            treeTable.setRoot(rootItem);
            rootItem.setExpanded(true);
        } catch (QblStorageException e) {
            e.printStackTrace();
        }

        treeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                selectedFolder = (TreeItem<BoxObject>) newValue);

        setCellValueFactories();
    }

    private void setCellValueFactories() {
        nameColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.NAME));
        sizeColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.SIZE));
        dateColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.MTIME));
        treeTable.getColumns().setAll(nameColumn, sizeColumn, dateColumn);
    }

    private BoxNavigation createSetup() throws QblStorageException {

        CryptoUtils utils = new CryptoUtils();
        byte[] deviceID = utils.getRandomBytes(16);
        DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

        QblECKeyPair testKey = new QblECKeyPair(Hex.decode(PRIVATE_KEY));

        this.volume = new BoxVolume(bucket, prefix, chain.getCredentials(), testKey, deviceID,
                new File(System.getProperty("java.io.tmpdir")));

        //DELETEME
        //cleanVolume();
        return volume.navigate();
    }

    @FXML
    protected void handleUploadFileButtonAction(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose File");
        List<File> list = chooser.showOpenMultipleDialog(treeTable.getScene().getWindow());
        if (list != null) {
            for (File file : list) {
                try {
                    uploadFiles(file, (BoxFolder) selectedFolder.getValue());
                } catch (QblStorageException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    @FXML
    protected void handleUploadFolderButtonAction(ActionEvent event) throws QblStorageException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Folder");
        File directory = chooser.showDialog(treeTable.getScene().getWindow());
        chooseUploadDirectory(directory);
    }

    @FXML
    protected void handleCreateFolderButtonAction(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog("name");
        dialog.setHeaderText(null);
        dialog.setTitle("Change Alias");
        dialog.setContentText("Please specify folder name");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                createFolder(name, (BoxFolder) selectedFolder.getValue());
            } catch (QblStorageException e) {
                alert("Failed to create Folder", e);
            }
        });
    }

    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) throws QblStorageException {
        if (selectedFolder.getParent() != null) {
            try {

                int n = JOptionPane.showConfirmDialog(
                        null,
                        "Delete " + selectedFolder.getValue().name + " ?",
                        "Delete?",
                        JOptionPane.YES_NO_OPTION);

                deleteBoxObject(n, (BoxObject) selectedFolder.getValue());
            } catch (QblStorageException e) {
                alert("Failed to create Folder", e);
            }
        }
    }

    void chooseUploadDirectory(File directory) {
        if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
            uploadedDirectory(directory, null);
        } else {
            uploadedDirectory(directory, (BoxFolder) selectedFolder.getValue());
        }
    }

    void uploadedDirectory(File directory, BoxFolder parentFolder) {
        File[] directoryFiles = directory.listFiles();
        try {
            BoxNavigation newNav;
            if (parentFolder == null) {
                newNav = nav;
            } else {
                newNav = nav.navigate(parentFolder);
            }
            BoxFolder boxDirectory = newNav.createFolder(directory.getName());
            newNav.commit();

            for (File f : directoryFiles) {
                if (f.listFiles() == null) {
                    BoxNavigation subNav = nav.navigate(boxDirectory);
                    subNav.upload(f.getName(), f);
                    subNav.commit();
                } else {
                    uploadedDirectory(f, boxDirectory);
                }
            }
        } catch (QblStorageException e) {
            e.printStackTrace();
        }
    }


    void createFolder(String name, BoxFolder folder) throws QblStorageException {
        BoxNavigation newNav = nav.navigate(folder);
        newNav.createFolder(name);
        newNav.commit();
    }

    void uploadFiles(File file, BoxFolder folder) throws QblStorageException {
        BoxNavigation newNav = nav.navigate(folder);
        newNav.upload(file.getName(), file);
        newNav.commit();
    }

    void deleteBoxObject(int n, BoxObject folder) throws QblStorageException {
        if (n == 0) {
            if (folder instanceof BoxFolder) {
                nav.delete((BoxFolder) folder);
                nav.commit();
            } else {
                nav.delete((BoxFile) folder);
                nav.commit();
            }
        }
    }

    private void cleanVolume() throws QblStorageException {
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
        volume.createIndex(bucket, prefix);
    }

}