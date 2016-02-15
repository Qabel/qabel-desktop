package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LazyBoxFolderTreeItem extends TreeItem<BoxObject> implements Observer {
	private BoxFolder folder;
	private ReadOnlyBoxNavigation navigation;
	private boolean upToDate;
	private boolean loading;
	private StringProperty nameProperty;
	private boolean isLeaf;
	private Image fileImg = new Image(getClass().getResourceAsStream("/file.png"));
	private static Image folderImg = new Image(LazyBoxFolderTreeItem.class.getResourceAsStream("/folder.png"));
	private static ExecutorService executorService = Executors.newCachedThreadPool();

	public LazyBoxFolderTreeItem(BoxFolder folder, ReadOnlyBoxNavigation navigation) {
		this(folder, navigation, folderImg);
	}

	public LazyBoxFolderTreeItem(BoxFolder folder, ReadOnlyBoxNavigation navigation, Image icon) {
		super(folder);
		ImageView value = new ImageView(icon);
		super.setGraphic(value);
		this.folder = folder;
		this.navigation = navigation;
		this.nameProperty = new SimpleStringProperty(folder.name);

		if (navigation instanceof Observable) {
			((Observable) navigation).addObserver(this);
		}
	}

	public Path getPath() {
		if (getParent() != null && getParent() instanceof LazyBoxFolderTreeItem) {
			return ((LazyBoxFolderTreeItem) getParent()).getPath().resolve(folder.name);
		}
		return Paths.get("/");
	}

	public boolean isLoading() {
		return loading;
	}

	public ReadOnlyBoxNavigation getNavigation() {
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
		nameProperty.set(folder.name + " (loading)");
		upToDate = true;

		Runnable updater = () -> {
			Collection<TreeItem<BoxObject>> col = null;
			String updateError = null;
			try {
				col = calculateChildren();
				LazyBoxFolderTreeItem.super.getChildren().setAll(col);
			} catch (QblStorageException e) {
				updateError = e.getMessage();
			} finally {
				isLeaf = super.getChildren().size() == 0;
				loading = false;
			}

			final Collection<TreeItem<BoxObject>> finalCol = col;
			final String finalUpdateError = updateError;
			Platform.runLater(() -> {
				if (finalCol == null) {
					nameProperty.set(folder.name + " (" + finalUpdateError + ")");
				} else {
					nameProperty.set(folder.name);
				}
			});
		};
		executorService.submit(updater);
	}

	private Collection<TreeItem<BoxObject>> calculateChildren() throws QblStorageException {
		List<TreeItem<BoxObject>> children = new LinkedList<>();
		for (BoxFolder folder : navigation.listFolders()) {
			children.add(new LazyBoxFolderTreeItem(folder, navigation.navigate(folder)));
		}

		for (BoxFile file : navigation.listFiles()) {
			children.add(new TreeItem<>(file, new ImageView(fileImg)));
		}
		return children;
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
		isLeaf = false;
		if (!isExpanded()) {
			return;
		}

		updateAsync();
	}

	@Override
	public String toString() {
		return folder.name;
	}
}
