package de.qabel.desktop.ui.remotefs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
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
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class RemoteFSController extends AbstractController implements Initializable {

	final String ROOT_FOLDER_NAME = "RootFolder";

	private QblECKeyPair keyPair;
	private String bucket;
	private String prefix;
	private BoxVolume volume;
	BoxNavigation nav;
	LazyBoxFolderTreeItem rootItem;
	TreeItem<BoxObject> selectedFolder;
	ResourceBundle resourceBundle;

	@Inject
	ClientConfiguration clientConfiguration;

	@Inject
	BoxVolumeFactory boxVolumeFactory;

	@FXML
	private TreeTableView<BoxObject> treeTable;
	@FXML
	private TreeTableColumn<BoxObject, String> nameColumn;
	@FXML
	private TreeTableColumn<BoxObject, String> sizeColumn;
	@FXML
	private TreeTableColumn<BoxObject, String> dateColumn;


	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
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
		keyPair = clientConfiguration.getSelectedIdentity().getPrimaryKeyPair();
		bucket = "qabel";

		prefix = MagicEvilPrefixSource.getPrefix(account);

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
		volume = boxVolumeFactory.getVolume(clientConfiguration.getAccount(), clientConfiguration.getSelectedIdentity());

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
		String title = resourceBundle.getString("chooseFile");
		chooser.setTitle(title);
		List<File> list = chooser.showOpenMultipleDialog(treeTable.getScene().getWindow());
		for (File file : list) {
			BoxFolder boxFolder = null;
			if (!(selectedFolder == null) && !selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
				boxFolder = (BoxFolder) selectedFolder.getValue();
			}

			try {
				uploadFiles(file, boxFolder);
			} catch (QblStorageException e) {
				e.printStackTrace();
			}
		}
		refreshTreeItem();
	}

	@FXML
	protected void handleUploadFolderButtonAction(ActionEvent event) throws QblStorageException {
		DirectoryChooser chooser = new DirectoryChooser();
		String title = resourceBundle.getString("chooseFolder");
		chooser.setTitle(title);
		File directory = chooser.showDialog(treeTable.getScene().getWindow());
		chooseUploadDirectory(directory);
		refreshTreeItem();
	}

	@FXML
	protected void handleDownloadButtonAction(ActionEvent event) throws QblStorageException, IOException {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(resourceBundle.getString("downloadFolder"));
		File directory = chooser.showDialog(treeTable.getScene().getWindow());
		BoxFolder parent = (BoxFolder) rootItem.getValue();
		BoxObject boxObject = selectedFolder.getValue();

		if (!selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
			parent = (BoxFolder) selectedFolder.getParent().getValue();
		}

		downloadBoxObject(boxObject, parent, directory.getPath());
		refreshTreeItem();
	}

	@FXML
	protected void handleCreateFolderButtonAction(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog(resourceBundle.getString("name"));
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("createFolder"));
		dialog.setContentText(resourceBundle.getString("folderName"));
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			BoxFolder boxFolder = null;
			if (!(selectedFolder == null) && !selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
				boxFolder = (BoxFolder) selectedFolder.getValue();
			}

			try {
				createFolder(name, boxFolder);
			} catch (QblStorageException e) {
				e.printStackTrace();
			}
		});
		refreshTreeItem();
	}

	@FXML
	protected void handleDeleteButtonAction(ActionEvent event) throws QblStorageException {
		if (selectedFolder.getParent() != null) {

			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle(resourceBundle.getString("deleteQuestion"));
			alert.setHeaderText(resourceBundle.getString("deleteFolder") + selectedFolder.getValue().name + " ?");
			Optional<ButtonType> result = alert.showAndWait();

			BoxFolder parent = null;
			LazyBoxFolderTreeItem updateTreeItem = (LazyBoxFolderTreeItem) selectedFolder.getParent();


			if (!(selectedFolder == null) && !selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
				parent = (BoxFolder) selectedFolder.getParent().getValue();
			}
			deleteBoxObject(result.get(), selectedFolder.getValue(), parent);
			rootItem.setUpToDate(false);
			rootItem.getChildren();
		}
	}


	void chooseUploadDirectory(File directory) {
		BoxFolder parent = null;

		if (!(selectedFolder == null) && !selectedFolder.getValue().name.equals(ROOT_FOLDER_NAME)) {
			parent = (BoxFolder) selectedFolder.getValue();
		}
		uploadedDirectory(directory, parent);
	}


	void uploadedDirectory(File directory, BoxFolder parentFolder) {
		File[] directoryFiles = directory.listFiles();
		try {
			BoxNavigation newNav = getNavigator(parentFolder);
			BoxFolder boxDirectory = newNav.createFolder(directory.getName());
			newNav.commit();

			for (File f : directoryFiles) {
				if (f.isDirectory()) {
					uploadedDirectory(f, boxDirectory);
				} else {
					BoxNavigation subNav = nav.navigate(boxDirectory);
					subNav.upload(f.getName(), f);
					subNav.commit();
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
			} else {
				newNav.delete((BoxFile) object);
			}
			newNav.commit();
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
		BoxNavigation newNav = nav;

		if (!(folder == null) && !folder.name.equals(ROOT_FOLDER_NAME)) {
			newNav = nav.navigate(folder);
		}

		return newNav;
	}

	private void refreshTreeItem() {

		LazyBoxFolderTreeItem parent = rootItem;
		LazyBoxFolderTreeItem currentNode = null;

		if (selectedFolder.getValue() instanceof BoxFolder && selectedFolder.getParent() != null) {

			currentNode = (LazyBoxFolderTreeItem) selectedFolder;
			parent = (LazyBoxFolderTreeItem) currentNode.getParent();

			currentNode.setExpanded(true);
			currentNode.setUpToDate(false);
			currentNode.getChildren();
		}
		parent.setExpanded(true);
		parent.setUpToDate(false);
		parent.getChildren();
	}

	ResourceBundle getRessource(){
		return resourceBundle;
	}

}