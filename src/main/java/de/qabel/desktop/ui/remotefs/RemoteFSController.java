package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.core.config.Identity;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.cellValueFactory.BoxObjectCellValueFactory;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.ShareNotifications;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.daemon.management.ManualDownload;
import de.qabel.desktop.daemon.management.ManualUpload;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.management.Upload;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.DetailsController;
import de.qabel.desktop.ui.DetailsView;
import de.qabel.desktop.ui.remotefs.factory.BoxNameCell;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.qabel.desktop.daemon.management.Transaction.TYPE.CREATE;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.DELETE;
import static javafx.scene.Cursor.HAND;


public class RemoteFSController extends AbstractController implements Initializable {
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    final String ROOT_FOLDER_NAME = "/";
    public static final int OPTION_EDGE_SIZE = 16;
    private static Image uploadFileImage = optionImage("/icon/upload.png");
    private static Image uploadFolderImage = optionImage("/icon/folder-upload.png");
    private static Image downloadImage = optionImage("/icon/download.png");
    private static Image addFolderImage = optionImage("/icon/add_folder.png");
    private static Image deleteImage = optionImage("/icon/delete.png");
    private static Image shareImage = optionImage("/icon/share.png");
    private FakeBoxObject shareObject;

    private static Image optionImage(String resourcePath) {
        return new Image(RemoteFSController.class.getResourceAsStream(resourcePath), OPTION_EDGE_SIZE, OPTION_EDGE_SIZE, true, true);
    }

    private BoxVolume volume;
    ReadableBoxNavigation nav;
    FilterableFolderTreeItem rootItem;
    TreeItem<BoxObject> virtualRoot;
    VirtualShareTreeItem shareRoot;
    ObjectProperty<TreeItem<BoxObject>> hoveredItem = new SimpleObjectProperty<>(null);
    ResourceBundle resourceBundle;

    @Inject
    ClientConfig clientConfiguration;
    @Inject
    BoxVolumeFactory boxVolumeFactory;
    @Inject
    TransferManager transferManager;
    @Inject
    DropMessageRepository dropMessageRepository;
    @Inject
    SharingService sharingService;

    @FXML
    private TreeTableView<BoxObject> treeTable;
    @FXML
    private TreeTableColumn<BoxObject, String> nameColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> sizeColumn;
    @FXML
    private TreeTableColumn<BoxObject, String> dateColumn;
    @FXML
    private TreeTableColumn<BoxObject, Node> optionsColumn;
    @FXML
    private StackPane stack;
    @FXML
    TextField searchQuery;

    DetailsController details;
    RemoteFileDetailsController fileDetails;

    private Identity identity;
    private ShareNotifications notifications;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Cursor oldCursor = stack.getCursor();
        stack.setCursor(Cursor.WAIT);
        resourceBundle = resources;
        observeIdentityChanges();
        initTreeTableView();


        setCellValueFactories();
        nameColumn.prefWidthProperty().bind(
            treeTable.widthProperty()
                .subtract(sizeColumn.widthProperty())
                .subtract(dateColumn.widthProperty())
                .subtract(optionsColumn.widthProperty())
                .subtract(2)
        );

        treeTable.getSelectionModel().clearSelection();
        treeTable.getSelectionModel().selectedItemProperty().addListener((o, x1, value) -> {
            if (value == null) {
                Platform.runLater(details::hide);
            }
        });

        DetailsView detailsView = new DetailsView();
        details = (DetailsController) detailsView.getPresenter();
        detailsView.getViewAsync(stack.getChildren()::add);
        stack.setCursor(oldCursor);
    }

    private void showDetails(TreeItem<BoxObject> value) {
        RemoteFileDetailsView view = new RemoteFileDetailsView(getNavigation(value), value.getValue());
        fileDetails = (RemoteFileDetailsController) view.getPresenter();
        view.getViewAsync(details::show);
    }

    private BoxNavigation getNavigation(TreeItem<BoxObject> value) {
        if (value instanceof FolderTreeItem) {
            return (BoxNavigation) ((FolderTreeItem) value).getNavigation();
        } else {
            return (BoxNavigation) ((FolderTreeItem)value.getParent()).getNavigation();
        }
    }

    private void observeIdentityChanges() {
        clientConfiguration.onSelectIdentity(i -> initTreeTableView());
    }

    private void initTreeTableView() {
        try {
            identity = clientConfiguration.getSelectedIdentity();
            notifications = clientConfiguration.getShareNotification(identity);
            nav = createSetup();
            virtualRoot = new StaticTreeItemContainer(new FakeBoxObject("virtualRoot"), null);

            shareObject = new FakeBoxObject("Shares");
            shareRoot = new VirtualShareTreeItem(
                sharingService,
                volume.getReadBackend(),
                notifications,
                shareObject
            );

            virtualRoot.getChildren().add(shareRoot);

            rootItem = new FilterableFolderTreeItem(
                    new BoxFolder(
                            volume.getRootRef(),
                            ROOT_FOLDER_NAME,
                            new byte[16]
                    ),
                    nav
            );

            searchQuery.textProperty().addListener((observable, oldValue, newValue) -> {
                Timer timer = new Timer();
                stack.setCursor(Cursor.WAIT);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!searchQuery.textProperty().get().equals(newValue)) {
                            return;
                        }
                        searchExecutor.submit(() -> rootItem.filterProperty().setValue(newValue));
                        searchExecutor.submit(() -> {
                            if (searchQuery.textProperty().get().equals(newValue)) {
                                stack.setCursor(null);
                            }
                        });
                    }
                }, 500);
            });

            rootItem.setExpanded(true);
            virtualRoot.getChildren().add(rootItem);
            virtualRoot.setExpanded(true);

            treeTable.setShowRoot(false);
            treeTable.setRoot(virtualRoot);

            if (nav instanceof PathNavigation) {
                Thread poller = new Thread(() -> {
                    try {
                        while (!Thread.interrupted()) {
                            try {
                                ((CachedBoxNavigation)nav).refresh();
                            } catch (QblStorageException e) {
                                e.printStackTrace();
                            }
                            shareRoot.refresh();
                            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                        }
                    } catch (InterruptedException ignored) {
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
        nameColumn.setCellFactory(ttc -> new BoxNameCell(shareObject));

        treeTable.setRowFactory(sharingRowFactory());
        optionsColumn.setCellValueFactory(inlineOptionsCellValueFactory());
    }

    private Callback<TreeTableColumn.CellDataFeatures<BoxObject, Node>, ObservableValue<Node>> inlineOptionsCellValueFactory() {
        return param -> {
            TreeItem<BoxObject> item = param.getValue();
            HBox bar = new HBox(3);
            SimpleObjectProperty<Node> result = new SimpleObjectProperty<>(bar);
            if (!(item.getValue() instanceof BoxFolder) && !(item.getValue() instanceof BoxFile)) {
                return result;
            }

            loadInlineButtons(item, bar);

            BoxObject value = item.getValue();
            TreeItem<BoxObject> folder = value instanceof BoxFolder ? item : item.getParent();
            if (!(folder instanceof FolderTreeItem)) {
                return result;
            }
            ReadableBoxNavigation rNav = ((FolderTreeItem)folder).getNavigation();
            if (!(rNav instanceof CachedBoxNavigation)) {
                return result;
            }

            CachedBoxNavigation nav = (CachedBoxNavigation)rNav;
            nav.addObserver((o, arg) -> {
                    if (!(arg instanceof ChangeEvent)) {
                        return;
                    }
                    ChangeEvent event = (ChangeEvent) arg;
                    if (!event.getPath().equals(value instanceof BoxFolder ? nav.getPath() : nav.getPath(item.getValue()))) {
                        return;
                    }
                    Platform.runLater(() -> {
                        try {
                            if (value instanceof BoxFile) {
                                item.setValue(nav.getFile(value.getName()));
                            }
                            loadInlineButtons(item, bar);
                            result.set(bar);
                        } catch (QblStorageException ignored) {
                        }
                    });
            });

            return result;
        };
    }

    private void loadInlineButtons(TreeItem<BoxObject> item, HBox bar) {
        bar.getChildren().clear();
        buttonFromImage(item, bar, downloadImage, this::download, "download");

        if (item.getValue() instanceof BoxFolder) {
            buttonFromImage(item, bar, uploadFileImage, this::uploadFile, "upload_file");
            buttonFromImage(item, bar, uploadFolderImage, this::uploadFolder, "upload_folder");
            buttonFromImage(item, bar, addFolderImage, this::createFolder, "create_folder");
        } else {
            spacer(bar);
            spacer(bar);
            spacer(bar);
        }

        buttonFromImage(item, bar, deleteImage, this::deleteItem, "delete");
        if (item.getValue() instanceof BoxFolder) {
            spacer(bar);
        } else if (!(item.getValue() instanceof BoxExternal)) {
            if (((BoxFile)item.getValue()).isShared()) {
                buttonFromImage(item, bar, shareImage, this::showDetails, "share", new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return true;
                    }
                }).getStyleClass().add("highlighted");
            } else {
                buttonFromImage(item, bar, shareImage, this::showDetails, "share");
            }
        }
    }

    private Callback<TreeTableView<BoxObject>, TreeTableRow<BoxObject>> sharingRowFactory() {
        return param1 -> {
            TreeTableRow<BoxObject> row = new TreeTableRow<>();
            row.hoverProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    hoveredItem.set(row.getTreeItem());
                } else if (oldValue && hoveredItem.get() == row.getTreeItem()) {
                    hoveredItem.set(null);
                }
            });

            row.treeItemProperty().addListener((observable, oldValue, newValue) -> {
                ObservableList<String> styleClass = row.getStyleClass();

                styleClass.remove("child");
                styleClass.remove("root");
                styleClass.remove("share-root");

                if (newValue == rootItem) {
                    styleClass.add("root");
                } else if (newValue == shareRoot) {
                    styleClass.add("share-root");
                } else {
                    styleClass.add("child");
                }
            });
            return row;
        };
    }

    private void spacer(HBox bar) {
        Label label = new Label();
        label.setPrefWidth(OPTION_EDGE_SIZE);
        bar.getChildren().add(label);
    }

    private void buttonFromImage(TreeItem<BoxObject> item, HBox bar, Image image, Consumer<TreeItem<BoxObject>> handler, String name) {
        buttonFromImage(item, bar, image, handler, name, hoveredItem.isEqualTo(item));
    }

    private Pane buttonFromImage(TreeItem<BoxObject> item, HBox bar, Image image, Consumer<TreeItem<BoxObject>> handler, String name, BooleanBinding showIf) {
        ImageView buttonIcon = new ImageView(image);
        Pane button = new Pane(buttonIcon);
        ObservableList<String> styleClass = button.getStyleClass();
        styleClass.add("inline-button");
        styleClass.add("faded-button");
        button.setCursor(HAND);
        button.setOnMouseClicked(event -> handler.accept(item));
        button.visibleProperty().bind(showIf);
        button.setId(name + "_" + treeTable.getRow(item));
        Tooltip tooltip = new Tooltip(resourceBundle.getString("option_" + name + "_tooltip"));
        Tooltip.install(button, tooltip);
        bar.getChildren().add(button);
        return button;
    }

    private ReadableBoxNavigation createSetup() throws QblStorageException {
        volume = boxVolumeFactory.getVolume(clientConfiguration.getAccount(), clientConfiguration.getSelectedIdentity());
        nav = volume.navigate();

        return nav;
    }

    private void uploadFile(TreeItem<BoxObject> item) {
        if (!(item instanceof FolderTreeItem)) {
            return;
        }

        FileChooser chooser = new FileChooser();
        String title = resourceBundle.getString("remoteFsChooseFile");
        chooser.setTitle(title);
        List<File> list = chooser.showOpenMultipleDialog(treeTable.getScene().getWindow());
        for (File file : list) {
            BoxPath destination = ((FolderTreeItem) item).getPath().resolve(file.getName());
            Path source = file.toPath();
            upload(source, destination);
        }
    }

    void upload(Path source, BoxPath destination) {
        Upload upload = new ManualUpload(CREATE, volume, source, destination);
        transferManager.addUpload(upload);
    }

    private void uploadFolder(TreeItem<BoxObject> item) {
        if (item == null || !(item.getValue() instanceof BoxFolder) || !(item instanceof FolderTreeItem)) {
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        String title = resourceBundle.getString("chooseFolder");
        chooser.setTitle(title);
        File directory = chooser.showDialog(treeTable.getScene().getWindow());
        try {
            chooseUploadDirectory(directory, item);
        } catch (IOException e) {
            alert("failed to upload folder", e);
        }
    }

    DirectoryChooser directoryChooser;
    private void download(TreeItem<BoxObject> item) {
        try {
            directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(resourceBundle.getString("remoteFsDownloadFolder"));
            File directory = directoryChooser.showDialog(treeTable.getScene().getWindow());
            BoxObject boxObject = item.getValue();
            if (directory == null) {
                return;
            }

            if (item instanceof ExternalTreeItem) {
                    if (!(boxObject instanceof BoxFile)) {  //TODO implement directory shares
                        return;
                    }
                    sharingService.downloadShare(
                            (BoxExternalFile)boxObject,
                            ((ExternalTreeItem) item).getNotification(),
                            directory.toPath().resolve(boxObject.getName()),
                            volume.getReadBackend()
                    );
            } else {
                BoxPath path;
                FolderTreeItem folderTreeItem;
                if (item.getValue() instanceof BoxFolder) {
                    folderTreeItem = (FolderTreeItem) item;
                    path = folderTreeItem.getPath();
                } else {
                    folderTreeItem = (FolderTreeItem) item.getParent();
                    path = folderTreeItem.getPath().resolve(boxObject.getName());
                }
                ReadableBoxNavigation navigation = folderTreeItem.getNavigation();

                downloadBoxObject(boxObject, navigation, path, directory.toPath());
            }
        } catch (Exception e) {
            alert(e);
        }
    }

    private void createFolder(TreeItem<BoxObject> item) {
        if (item == null || !(item instanceof FolderTreeItem)) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(resourceBundle.getString("remoteFsName"));
        dialog.setHeaderText(null);
        dialog.setTitle(resourceBundle.getString("remoteFsCreateFolder"));
        dialog.setContentText(resourceBundle.getString("remoteFsFolderName"));
        Optional<String> result = dialog.showAndWait();
        new Thread(() -> {
            result.ifPresent(name -> {
                FolderTreeItem lazyItem = (FolderTreeItem) item;

                try {
                    createFolder(lazyItem.getPath().resolve(name));
                } catch (QblStorageException e) {
                    alert("Failed to create Folder", e);
                }
            });
        }).start();
    }

    private void deleteItem(TreeItem<BoxObject> item) {
        try {
            if (item instanceof ExternalTreeItem) {
                clientConfiguration.getShareNotification(clientConfiguration.getSelectedIdentity())
                        .remove(((ExternalTreeItem) item).getNotification());
                return;
            }
            if (item.getParent() != null) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(resourceBundle.getString("remoteFsDeleteQuestion"));
                alert.setHeaderText(resourceBundle.getString("remoteFsDeleteFolder") + item.getValue().getName() + " ?");
                Optional<ButtonType> result = alert.showAndWait();

                FolderTreeItem updateTreeItem = (FolderTreeItem) item.getParent();
                BoxPath path = updateTreeItem.getPath().resolve(item.getValue().getName());

                deleteBoxObject(result.get(), path, item.getValue());
            }
        } catch (QblStorageException e) {
            alert(e);
        }
    }

    void chooseUploadDirectory(File directory, TreeItem<BoxObject> item) throws IOException {
        BoxPath destination = BoxFileSystem.get(((FolderTreeItem) item).getPath()).resolve(directory.getName());
        uploadDirectory(directory.toPath(), destination);
    }

    void uploadDirectory(Path source, BoxPath destination) throws IOException {

        Files.walkFileTree(source, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                transferManager.addUpload(new ManualUpload(CREATE, volume, dir, resolveDestination(dir), true));
                return FileVisitResult.CONTINUE;
            }

            private BoxPath resolveDestination(Path dir) {
                return destination.resolve(source.relativize(dir));
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                transferManager.addUpload(new ManualUpload(CREATE, volume, file, resolveDestination(file), false));
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


    void createFolder(BoxPath destination) throws QblStorageException {
        transferManager.addUpload(new ManualUpload(CREATE, volume, null, destination, true));
    }

    void deleteBoxObject(ButtonType confim, BoxPath path, BoxObject object) throws QblStorageException {
        if (confim != ButtonType.OK) {
            return;
        }

        transferManager.addUpload(new ManualUpload(DELETE, volume, null, path, object instanceof BoxFolder));
    }

    void downloadBoxObject(BoxObject boxObject, ReadableBoxNavigation nav, BoxPath source, Path destination) throws QblStorageException {
        destination = destination.resolve(boxObject.getName());
        if (boxObject instanceof BoxFile) {
            downloadFile((BoxFile)boxObject, nav, source, destination);
        } else {
            downloadBoxFolder(nav, source, destination);
        }
    }

    private void downloadBoxFolder(ReadableBoxNavigation nav, BoxPath source, Path destination) throws QblStorageException {
        transferManager.addDownload(new ManualDownload(CREATE, volume, source, destination, true));

        for (BoxFile file : nav.listFiles()) {
            downloadFile(file, nav, source.resolve(file.getName()), destination.resolve(file.getName()));
        }
        for (BoxFolder folder : nav.listFolders()) {
            downloadBoxFolder(nav.navigate(folder), source.resolve(folder.getName()), destination.resolve(folder.getName()));
        }
    }

    private void downloadFile(BoxFile file, ReadableBoxNavigation nav, BoxPath source, Path destination) {
        transferManager.addDownload(new ManualDownload(file.getMtime(), CREATE, volume, source, destination, false));
    }

    private ReadableBoxNavigation getNavigator(BoxFolder folder) throws QblStorageException {
        ReadableBoxNavigation newNav = nav;

        if (!(folder == null) && !folder.getName().equals(ROOT_FOLDER_NAME)) {
            newNav = nav.navigate(folder);
        }

        return newNav;
    }

    ResourceBundle getRessource(){
        return resourceBundle;
    }
}
