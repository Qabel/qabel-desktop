package de.qabel.desktop.ui.remotefs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
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

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class RemoteFSController extends AbstractController implements Initializable {

    private QblECKeyPair KEY_PAIR;
    public String ROOT_FOLDER_NAME = "RootFolder";
    private String bucket;
    private String prefix;
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

        createObserver();
        initTreeTableView();

        treeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                selectedFolder = (TreeItem<BoxObject>) newValue;
            }
        });

        setCellValueFactories();
    }

    private void createObserver() {
        clientConfiguration.addObserver((o, arg) -> {
            if (!(arg instanceof Identity)) {
                return;
            }
            initTreeTableView();
        });
    }

    private void initTreeTableView() {

        Account account = clientConfiguration.getAccount();
        AccountingHTTP http = null;
        KEY_PAIR = clientConfiguration.getSelectedIdentity().getPrimaryKeyPair();
        bucket = "qabel";

        try {
            http = new AccountingHTTP(new AccountingServer(new URL(account.getProvider()).toURI(), account.getUser(), account.getAuth()), new AccountingProfile());
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            http.login();
            http.updatePrefixes();
           // prefix = http.getPrefixes().get(0);
            prefix = "prefix";
        } catch (IOException | QblInvalidCredentials e) {
            e.printStackTrace();
        }

        try {
            nav = createSetup();
        } catch (QblStorageException e) {
            e.printStackTrace();
        }
        rootItem = new LazyBoxFolderTreeItem(new BoxFolder("block", ROOT_FOLDER_NAME, new byte[16]), nav);
        treeTable.setRoot(rootItem);

        rootItem.setExpanded(true);
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


        this.volume = new BoxVolume(bucket, prefix, chain.getCredentials(), KEY_PAIR, deviceID,
                new File(System.getProperty("java.io.tmpdir")));

        try {
            nav = volume.navigate();
        } catch (QblStorageNotFound e) {
            volume.createIndex(bucket, prefix);
            nav = volume.navigate();
        }

        return nav;
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
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Download Folder");
        File directory = chooser.showDialog(treeTable.getScene().getWindow());

        if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
            downloadBoxObjectRootNode(selectedFolder.getValue(), null, directory.getPath());
        } else {
            LazyBoxFolderTreeItem parent = (LazyBoxFolderTreeItem) selectedFolder.getParent();
            downloadBoxObject(selectedFolder.getValue(), (BoxFolder) parent.getValue(), directory.getPath());
        }
        refreshTreeItem();
    }

    @FXML
    protected void handleCreateFolderButtonAction(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog("name");
        dialog.setHeaderText(null);
        dialog.setTitle("Create Folder");
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

                if (selectedFolder == null || selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
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
            BoxNavigation newNav = getNavigator(parent);

            if (object instanceof BoxFolder) {
                newNav.delete((BoxFolder) object);
                newNav.commit();
            } else {
                newNav.delete((BoxFile) object);
                newNav.commit();
            }
        }
    }

    void downloadBoxObject(BoxObject boxObject, BoxFolder parent, String path) throws QblStorageException, IOException {
        if (boxObject instanceof BoxFile) {
            saveFile((BoxFile) boxObject, parent, path);
        } else {
            downloadBoxFolder((BoxFolder) boxObject, parent, path);
        }
    }


    void downloadBoxObjectRootNode(BoxObject boxFolder, BoxFolder parent, String path) throws QblStorageException, IOException {
        List<BoxFile> files = nav.listFiles();
        List<BoxFolder> folders = nav.listFolders();
        File dir = new File(path + "/" + ROOT_FOLDER_NAME);

        reursiveDownload((BoxFolder) boxFolder, parent, files, folders, dir, dir.getPath());

    }


    private void downloadBoxFolder(BoxFolder boxFolder, BoxFolder parent, String path) throws QblStorageException, IOException {
        BoxNavigation newNav = nav.navigate(boxFolder);

        List<BoxFile> files = newNav.listFiles();
        List<BoxFolder> folders = newNav.listFolders();

        File dir = new File(path + "/" + boxFolder.name);
        reursiveDownload(boxFolder, parent, files, folders, dir, dir.getPath());
    }

    private void reursiveDownload(BoxFolder boxFolder, BoxFolder parent, List<BoxFile> files, List<BoxFolder> folders, File dir, String path) throws IOException, QblStorageException {
        if (dir.mkdirs()) {

            for (BoxFile f : files) {
                saveFile(f, parent, path);
            }

            for (BoxFolder bf : folders) {
                downloadBoxFolder(bf, boxFolder, path);
            }
        }
    }

    private void saveFile(BoxFile f, BoxFolder parent, String path) throws IOException, QblStorageException {

        BoxNavigation newNav = getNavigator(parent);
        InputStream file = newNav.download(f);

        byte[] buffer = new byte[file.available()];
        file.read(buffer);

        File targetFile = new File(path + "/" + f.name);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
    }


    private BoxNavigation getNavigator(BoxFolder folder) throws QblStorageException {
        BoxNavigation newNav;
        if (folder == null || folder.name == ROOT_FOLDER_NAME) {
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