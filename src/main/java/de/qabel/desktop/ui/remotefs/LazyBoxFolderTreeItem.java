package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LazyBoxFolderTreeItem extends TreeItem<BoxObject> {
	private BoxFolder folder;
	private BoxNavigation navigation;
	private boolean upToDate;
	private boolean loading;
	private StringProperty nameProperty;
	private boolean isLeaf;
	private Image fileImg = new Image(getClass().getResourceAsStream("/file.png"));
	private static Image folderImg = new Image(LazyBoxFolderTreeItem.class.getResourceAsStream("/folder.png"));

	public LazyBoxFolderTreeItem(BoxFolder folder, BoxNavigation navigation) {
		this(folder, navigation, folderImg);
	}

	public LazyBoxFolderTreeItem(BoxFolder folder, BoxNavigation navigation, Image icon) {
		super(folder);
		ImageView value = new ImageView(icon);
		super.setGraphic(value);

		this.folder = folder;
		this.navigation = navigation;
		this.nameProperty = new SimpleStringProperty(folder.name);
	}

	public boolean isLoading() {
		return loading;
	}

	BoxNavigation getNavigation() {
		return navigation;
	}

	@Override
	public ObservableList<TreeItem<BoxObject>> getChildren() {
		if (!upToDate) {
			loading = true;
			nameProperty.set(folder.name + " (loading)");
			upToDate = true;
			Platform.runLater(() -> {
				try {
					LazyBoxFolderTreeItem.super.getChildren().setAll(calculateChildren());
					isLeaf = super.getChildren().size() == 0;
				} catch (QblStorageException e) {
					nameProperty.set(folder.name + " (" + e.getMessage() + ")");
				} finally {
					nameProperty.set(folder.name);
					loading = false;
				}
			});
		}
		return super.getChildren();
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

	public void setUpToDate(Boolean upToDate){
		this.upToDate = upToDate;
	}
}
