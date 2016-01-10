package de.qabel.desktop.ui.remotefs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class RemoteFSController extends AbstractController implements Initializable {

    private String PRIVATE_KEY;
    public String ROOT_FOLDER_NAME = "root Folder";
    private String bucket = "qabel";
    private String prefix = "qabelTest";
    private BoxVolume volume;
    BoxNavigation nav;
    LazyBoxFolderTreeItem rootItem;
    TreeItem<BoxObject> selectedFolder;
    @FXML
    private TreeTableView<BoxObject> treeTable;
    @FXML
    private TreeTableColumn<BoxObject, String> nameColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> sizeColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> dateColumn;
    @Inject
    ClientConfiguration clientConfiguration;

    @FXML

    public void initialize(URL location, ResourceBundle resources) {

        if (clientConfiguration.hasAccount()) {
            PRIVATE_KEY = clientConfiguration.getSelectedIdentity().getPrimaryKeyPair().getPrivateKey().toString();
        } else {
            PRIVATE_KEY = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a";
        }

        try {
            nav = createSetup();
            rootItem = new LazyBoxFolderTreeItem(new BoxFolder("block", ROOT_FOLDER_NAME, new byte[16]), nav);
            treeTable.setRoot(rootItem);
            rootItem.setExpanded(true);
        } catch (QblStorageException e) {
            e.printStackTrace();
        }

        treeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                selectedFolder = (TreeItem<BoxObject>) newValue;
            }
        });

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
                    if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
                        uploadFiles(file, null);
                    } else {
                        uploadFiles(file, (BoxFolder) selectedFolder.getValue());
                    }
                } catch (QblStorageException e) {
                    e.printStackTrace();
                }

            }
        }
        refreshTreeItem();
    }

    @FXML
    protected void handleUploadFolderButtonAction(ActionEvent event) throws QblStorageException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Folder");
        File directory = chooser.showDialog(treeTable.getScene().getWindow());
        chooseUploadDirectory(directory);
        refreshTreeItem();
    }

    @FXML
    protected void handleDownloadButtonAction(ActionEvent event) throws QblStorageException, IOException {
        if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
            downloadBoxObject(selectedFolder.getValue(), null);
        } else {
            LazyBoxFolderTreeItem parent = (LazyBoxFolderTreeItem) selectedFolder.getParent();
            downloadBoxObject(selectedFolder.getValue(), (BoxFolder) parent.getValue());
        }
        refreshTreeItem();
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
                if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
                    createFolder(name, null);
                } else {
                    createFolder(name, (BoxFolder) selectedFolder.getValue());
                }
            } catch (QblStorageException e) {
                e.printStackTrace();
            }
        });
        refreshTreeItem();
    }

    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) throws QblStorageException {
        if (selectedFolder.getParent() != null) {
            try {
                LazyBoxFolderTreeItem parent = (LazyBoxFolderTreeItem) selectedFolder.getParent();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete?");
                alert.setHeaderText("Delete " + selectedFolder.getValue().name + " ?");
                Optional<ButtonType> result = alert.showAndWait();

                if (!(selectedFolder == null) || !selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
                    if (!selectedFolder.getParent().getValue().name.equals(ROOT_FOLDER_NAME)) {
                        deleteBoxObject(result.get(), selectedFolder.getValue(), (BoxFolder) parent.getValue());
                    } else {
                        deleteBoxObject(result.get(), selectedFolder.getValue(), null);
                    }
                    rootItem.setUpToDate(false);
                    rootItem.getChildren();
                }
            } catch (QblStorageException e) {
                e.printStackTrace();
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
            newNav = getNavigator(parentFolder);
            BoxFolder boxDirectory = newNav.createFolder(directory.getName());
            newNav.commit();
            if (directoryFiles != null) {
                for (File f : directoryFiles) {
                    if (f.listFiles() == null) {
                        BoxNavigation subNav = nav.navigate(boxDirectory);
                        subNav.upload(f.getName(), f);
                        subNav.commit();
                    } else {
                        uploadedDirectory(f, boxDirectory);
                    }
                }
            }
        } catch (QblStorageException e) {
            e.printStackTrace();

        }
    }


    void createFolder(String name, BoxFolder folder) throws QblStorageException {

        BoxNavigation newNav;
        newNav = getNavigator(folder);
        newNav.createFolder(name);
        newNav.commit();
    }


    void uploadFiles(File file, BoxFolder folder) throws QblStorageException {
        BoxNavigation newNav;
        newNav = getNavigator(folder);
        newNav.upload(file.getName(), file);
        newNav.commit();
    }

    void deleteBoxObject(ButtonType confim, BoxObject object, BoxFolder parent) throws QblStorageException {
        if (confim == ButtonType.OK) {
            BoxNavigation newNav;
            if (parent != null) {
                newNav = nav.navigate(parent);
            } else {
                newNav = nav;
            }
            if (object instanceof BoxFolder) {
                newNav.delete((BoxFolder) object);
                newNav.commit();
            } else {
                newNav.delete((BoxFile) object);
                newNav.commit();
            }
        }
    }

    private void downloadBoxObject(BoxObject boxObject, BoxFolder parent) throws QblStorageException, IOException {
        String path = "tmp";
        if (boxObject instanceof BoxFile) {
            saveFile((BoxFile) boxObject, parent, path);
        } else {
            downloadBoxFolder((BoxFolder) boxObject, parent, path);
        }
    }

    private void downloadBoxFolder(BoxFolder boxFolder, BoxFolder parent, String path) throws QblStorageException, IOException {
        BoxNavigation newNav = getNavigator(parent);
        List<BoxFile> files = newNav.listFiles();
        List<BoxFolder> folders = newNav.listFolders();

        File dir = new File(path+"/"+boxFolder.name);
        dir.mkdirs();

        for (BoxFile f: files) {
            saveFile(f, parent, dir.getPath());
        }
        for (BoxFolder bf:folders) {
            downloadBoxFolder(bf, boxFolder, path+"/"+bf.name);
        }

    }

    private void saveFile(BoxFile f, BoxFolder parent, String path) throws IOException, QblStorageException {

        BoxNavigation newNav = getNavigator(parent);
        InputStream file = newNav.download(f);

        byte[] buffer = new byte[file.available()];
        file.read(buffer);

        File targetFile = new File(path+"/"+f.name);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
    }


    private BoxNavigation getNavigator(BoxFolder folder) throws QblStorageException {
        BoxNavigation newNav;
        if (folder == null) {
            newNav = nav;
        } else {
            newNav = nav.navigate(folder);
        }
        return newNav;
    }

    private void refreshTreeItem() {

        if (selectedFolder.getValue() instanceof BoxFolder) {
            LazyBoxFolderTreeItem currentNode = (LazyBoxFolderTreeItem) selectedFolder;
            LazyBoxFolderTreeItem parent = (LazyBoxFolderTreeItem) currentNode.getParent();

            currentNode.setUpToDate(false);
            currentNode.getChildren();

            if (parent != null) {
                parent.setUpToDate(false);
                parent.getChildren();
            }
        } else {
            LazyBoxFolderTreeItem parent = (LazyBoxFolderTreeItem) selectedFolder.getParent();
            if (parent != null) {
                parent.setUpToDate(false);
                parent.getChildren();
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