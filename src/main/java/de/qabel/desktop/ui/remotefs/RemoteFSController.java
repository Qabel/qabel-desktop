package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static de.qabel.desktop.daemon.management.Transaction.TYPE.CREATE;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.DELETE;


public class RemoteFSController extends AbstractController implements Initializable {
	final String ROOT_FOLDER_NAME = "RootFolder";

	private QblECKeyPair keyPair;
	private String bucket;
	private BoxVolume volume;
	ReadOnlyBoxNavigation nav;
	LazyBoxFolderTreeItem rootItem;
	TreeItem<BoxObject> selectedItem;
	ResourceBundle resourceBundle;

	@Inject
	ClientConfiguration clientConfiguration;

	@Inject
	BoxVolumeFactory boxVolumeFactory;

	@Inject
	LoadManager loadManager;

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
				selectedItem = (TreeItem<BoxObject>) newValue;
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
		try {
			nav = createSetup();
		rootItem = new LazyBoxFolderTreeItem(new BoxFolder(volume.getRootRef(), ROOT_FOLDER_NAME, new byte[16]), nav);
		treeTable.setRoot(rootItem);
		rootItem.setExpanded(true);

		if (nav instanceof CachedBoxNavigation) {
			Thread poller = new Thread(() -> {
				try {
					while (!Thread.interrupted()) {
						try {
							((CachedBoxNavigation)nav).refresh();
						} catch (QblStorageException e) {
							e.printStackTrace();
						}
						Thread.sleep(TimeUnit.MINUTES.toMillis(1));
					}
				} catch (InterruptedException e) {
				} finally {
				}
			});
			poller.setDaemon(true);
			poller.start();
		}
		} catch (QblStorageException e) {
			alert("failed to load remotefs", e);
		}
	}


	private void setCellValueFactories() {
		nameColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.NAME));
		sizeColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.SIZE));
		dateColumn.setCellValueFactory(new BoxObjectCellValueFactory(BoxObjectCellValueFactory.MTIME));
		treeTable.getColumns().setAll(nameColumn, sizeColumn, dateColumn);
	}

	private ReadOnlyBoxNavigation createSetup() throws QblStorageException {
		volume = boxVolumeFactory.getVolume(clientConfiguration.getAccount(), clientConfiguration.getSelectedIdentity());
		nav = volume.navigate();

		return nav;
	}

	@FXML
	protected void handleUploadFileButtonAction(ActionEvent event) {
		if (!(selectedItem instanceof LazyBoxFolderTreeItem)) {
			return;
		}

		FileChooser chooser = new FileChooser();
		String title = resourceBundle.getString("chooseFile");
		chooser.setTitle(title);
		List<File> list = chooser.showOpenMultipleDialog(treeTable.getScene().getWindow());
		for (File file : list) {
			Path destination = ((LazyBoxFolderTreeItem) selectedItem).getPath().resolve(file.getName());
			Path source = file.toPath();
			upload(source, destination);
		}
	}

	void upload(Path source, Path destination) {
		Upload upload = new ManualUpload(CREATE, volume, source, destination);
		loadManager.addUpload(upload);
	}

	@FXML
	protected void handleUploadFolderButtonAction(ActionEvent event) throws QblStorageException {
		DirectoryChooser chooser = new DirectoryChooser();
		String title = resourceBundle.getString("chooseFolder");
		chooser.setTitle(title);
		File directory = chooser.showDialog(treeTable.getScene().getWindow());
		try {
			chooseUploadDirectory(directory);
		} catch (IOException e) {
			alert("failed to upload folder", e);
		}
	}

	@FXML
	protected void handleDownloadButtonAction(ActionEvent event) throws QblStorageException, IOException {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(resourceBundle.getString("downloadFolder"));
		File directory = chooser.showDialog(treeTable.getScene().getWindow());
		BoxObject boxObject = selectedItem.getValue();

		Path path;
		LazyBoxFolderTreeItem folderTreeItem;
		if (selectedItem instanceof LazyBoxFolderTreeItem) {
			folderTreeItem = (LazyBoxFolderTreeItem) this.selectedItem;
			path = folderTreeItem.getPath();
		} else {
			folderTreeItem = (LazyBoxFolderTreeItem) selectedItem.getParent();
			path = folderTreeItem.getPath().resolve(boxObject.name);
		}
		ReadOnlyBoxNavigation navigation = folderTreeItem.getNavigation();

		downloadBoxObject(boxObject, navigation, path, directory.toPath());
	}

	@FXML
	protected void handleCreateFolderButtonAction(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog(resourceBundle.getString("name"));
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("createFolder"));
		dialog.setContentText(resourceBundle.getString("folderName"));
		Optional<String> result = dialog.showAndWait();
		new Thread(() -> {
			result.ifPresent(name -> {
				BoxFolder boxFolder = null;
				if (!(selectedItem == null) && !selectedItem.getValue().name.equals(ROOT_FOLDER_NAME)) {
					boxFolder = (BoxFolder) selectedItem.getValue();
				}

				try {
					createFolder(name, boxFolder);
				} catch (QblStorageException e) {
					e.printStackTrace();
				}
			});
		}).start();
	}

	@FXML
	protected void handleDeleteButtonAction(ActionEvent event) throws QblStorageException {
		if (selectedItem.getParent() != null) {

			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle(resourceBundle.getString("deleteQuestion"));
			alert.setHeaderText(resourceBundle.getString("deleteFolder") + selectedItem.getValue().name + " ?");
			Optional<ButtonType> result = alert.showAndWait();

			LazyBoxFolderTreeItem updateTreeItem = (LazyBoxFolderTreeItem) selectedItem.getParent();
			Path path = updateTreeItem.getPath().resolve(selectedItem.getValue().name);

			deleteBoxObject(result.get(), path, selectedItem.getValue());
		}
	}


	void chooseUploadDirectory(File directory) throws IOException {

		if (selectedItem == null || !(selectedItem.getValue() instanceof BoxFolder) || !(selectedItem instanceof LazyBoxFolderTreeItem)) {
			return;
		}
		Path destination = Paths.get(((LazyBoxFolderTreeItem)selectedItem).getPath().toString(), directory.getName());
		uploadDirectory(directory.toPath(), destination);
	}

	void uploadDirectory(Path source, Path destination) throws IOException {

		Files.walkFileTree(source, new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				loadManager.addUpload(new ManualUpload(CREATE, volume, dir, resolveDestination(dir), true));
				return FileVisitResult.CONTINUE;
			}

			private Path resolveDestination(Path dir) {
				return destination.resolve(source.relativize(dir));
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				loadManager.addUpload(new ManualUpload(CREATE, volume, file, resolveDestination(file), false));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}


	void createFolder(String name, BoxFolder folder) throws QblStorageException {
		BoxNavigation newNav = (BoxNavigation)getNavigator(folder);
		newNav.createFolder(name);
		newNav.commit();
	}

	void deleteBoxObject(ButtonType confim, Path path, BoxObject object) throws QblStorageException {
		if (confim != ButtonType.OK) {
			return;
		}

		loadManager.addUpload(new ManualUpload(DELETE, volume, null, path, object instanceof BoxFolder));
	}

	void downloadBoxObject(BoxObject boxObject, ReadOnlyBoxNavigation nav, Path source, Path destination) throws QblStorageException, IOException {
		destination = destination.resolve(boxObject.name);
		if (boxObject instanceof BoxFile) {
			downloadFile((BoxFile)boxObject, nav, source, destination);
		} else {
			downloadBoxFolder(nav, source, destination);
		}
	}

	private void downloadBoxFolder(ReadOnlyBoxNavigation nav, Path source, Path destination) throws QblStorageException, IOException {
		loadManager.addDownload(new ManualDownload(CREATE, volume, source, destination, true));

		for (BoxFile file : nav.listFiles()) {
			downloadFile(file, nav, source.resolve(file.name), destination.resolve(file.name));
		}
		for (BoxFolder folder : nav.listFolders()) {
			downloadBoxFolder(nav.navigate(folder), source.resolve(folder.name), destination.resolve(folder.name));
		}
	}

	private void downloadFile(BoxFile file, ReadOnlyBoxNavigation nav, Path source, Path destination) throws IOException, QblStorageException {
		loadManager.addDownload(new ManualDownload(file.mtime, CREATE, volume, source, destination, false));
	}


	private ReadOnlyBoxNavigation getNavigator(BoxFolder folder) throws QblStorageException {
		ReadOnlyBoxNavigation newNav = nav;

		if (!(folder == null) && !folder.name.equals(ROOT_FOLDER_NAME)) {
			newNav = nav.navigate(folder);
		}

		return newNav;
	}

	ResourceBundle getRessource(){
		return resourceBundle;
	}
}
