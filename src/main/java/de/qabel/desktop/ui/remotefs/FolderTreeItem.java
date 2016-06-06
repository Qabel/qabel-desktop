package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FolderTreeItem extends TreeItem<BoxObject> implements Observer {
    private BoxFolder folder;
    private ReadableBoxNavigation navigation;
    private boolean upToDate;
    private boolean loading;
    private StringProperty nameProperty;
    private boolean isLeaf;
    private Image fileImg = new Image(getClass().getResourceAsStream("/icon/file.png"),  18, 18, true, false);
    private static Image folderImg = new Image(FolderTreeItem.class.getResourceAsStream("/icon/folder.png"), 18, 18, true, true);
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public FolderTreeItem(BoxFolder folder, ReadableBoxNavigation navigation) {
        this(folder, navigation, folderImg);
        getGraphic().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setExpanded(true);
            }
        });
    }

    public FolderTreeItem(BoxFolder folder, ReadableBoxNavigation navigation, Image icon) {
        super(folder);
        ImageView value = new ImageView(icon);
        super.setGraphic(value);
        this.folder = folder;
        this.navigation = navigation;
        nameProperty = new SimpleStringProperty(folder.getName());

        if (navigation instanceof Observable) {
            ((Observable) navigation).addObserver(this);
        }
    }

    public BoxPath getPath() {
        if (getParent() != null && getParent() instanceof FolderTreeItem) {
            return ((FolderTreeItem) getParent()).getPath().resolve(folder.getName());
        }
        return BoxFileSystem.getRoot();
    }

    public boolean isLoading() {
        return loading;
    }

    public ReadableBoxNavigation getNavigation() {
        return navigation;
    }

    @Override
    public ObservableList<TreeItem<BoxObject>> getChildren() {
        if (!upToDate) {
            updateAsync();
        }
        return super.getChildren();
    }

    protected void updateAsync() {
        loading = true;
        nameProperty.set(folder.getName() + " (loading)");
        upToDate = true;

        Runnable updater = () -> {
            Collection<TreeItem<BoxObject>> col = null;
            String updateError = null;
            try {
                col = calculateChildren();
                synchronized (this) {
                    Map<BoxObject, TreeItem<BoxObject>> newObjects = new HashMap<>();
                    col.stream().forEach(item -> newObjects.put(item.getValue(), item));
                    Map<BoxObject, TreeItem<BoxObject>> oldObjects = new HashMap<>();
                    ObservableList<TreeItem<BoxObject>> children = FolderTreeItem.super.getChildren();
                    children.stream().forEach(item -> oldObjects.put(item.getValue(), item));

                    oldObjects.keySet().stream()
                            .filter(object -> !newObjects.containsKey(object))
                            .forEach(object -> Platform.runLater(() -> children.remove(oldObjects.get(object))));
                    newObjects.keySet().stream()
                            .filter(object -> !oldObjects.containsKey(object))
                            .forEach(object -> Platform.runLater(() -> children.add(newObjects.get(object))));
                }
            } catch (QblStorageException e) {
                updateError = e.getMessage();
            } finally {
                Platform.runLater(() -> isLeaf = super.getChildren().size() == 0);
                loading = false;
            }

            final Collection<TreeItem<BoxObject>> finalCol = col;
            final String finalUpdateError = updateError;
            Platform.runLater(() -> {
                if (finalCol == null) {
                    nameProperty.set(folder.getName() + " (" + finalUpdateError + ")");
                } else {
                    nameProperty.set(folder.getName());
                }
                if (!isLeaf && isExpanded() && getChildren().size() == 1) {
                    TreeItem<BoxObject> child = getChildren().get(0);
                    if (child.getValue() instanceof BoxFolder && !child.expandedProperty().isBound()) {
                        child.setExpanded(true);
                    }
                }
            });
        };
        executorService.submit(updater);
    }

    private Collection<TreeItem<BoxObject>> calculateChildren() throws QblStorageException {
        List<TreeItem<BoxObject>> children = new LinkedList<>();
        for (BoxFolder folder : navigation.listFolders()) {
            BoxNavigation subNavigation = navigation.navigate(folder);
            children.add(initSubFolderItem(folder, subNavigation));
        }

        for (BoxFile file : navigation.listFiles()) {
            children.add(initSubFileItem(file));
        }
        return children;
    }

    protected FilterableTreeItem initSubFileItem(BoxFile file) {
        return new FilterableTreeItem(file, new ImageView(fileImg));
    }

    protected FolderTreeItem initSubFolderItem(BoxFolder folder, BoxNavigation subNavigation) {
        return new FolderTreeItem(folder, subNavigation);
    }

    @Override
    public boolean isLeaf() {
        return isLeaf;
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public void setUpToDate(Boolean upToDate) {
        this.upToDate = upToDate;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != navigation || !(arg instanceof RemoteChangeEvent)) {
            return;
        }
        RemoteChangeEvent event = (RemoteChangeEvent)arg;
        if (event.getBoxNavigation() != navigation) {
            return;
        }

        upToDate = false;
        if (!isExpanded() && !isLeaf()) {
            return;
        }
        isLeaf = false;

        updateAsync();
    }

    @Override
    public String toString() {
        return folder.getName();
    }
}
