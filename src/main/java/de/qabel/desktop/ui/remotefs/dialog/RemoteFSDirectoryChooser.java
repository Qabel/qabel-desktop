package de.qabel.desktop.ui.remotefs.dialog;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.ui.remotefs.BoxObjectTreeCell;
import de.qabel.desktop.ui.remotefs.LazyBoxFolderTreeItem;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.ResourceBundle;

public class RemoteFSDirectoryChooser extends Dialog<Path> {
	Button okButton;
	Button cancelButton;
	private ObjectPropertyBase<Path> selectedProperty = new SimpleObjectProperty<>(null);
	final TreeView<BoxObject> tree;
	final TreeItem<BoxObject> root;

	public RemoteFSDirectoryChooser(ResourceBundle resources, BoxVolume volume) throws QblStorageException {
		super();

		setTitle(resources.getString("chooseRemoteFolderToSync"));
		ButtonType okType = new ButtonType(resources.getString("open"), ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelType = new ButtonType(resources.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(okType);
		getDialogPane().getButtonTypes().add(cancelType);

		okButton = (Button) getDialogPane().lookupButton(okType);
		cancelButton = (Button) getDialogPane().lookupButton(cancelType);


		BoxNavigation nav = volume.navigate();
		root = new LazyBoxFolderTreeItem(new BoxFolder(volume.getRootRef(), "/", new byte[16]), nav);
		tree = new TreeView<>(root);
		getDialogPane().setContent(tree);

		tree.setCellFactory(param -> new BoxObjectTreeCell());
		tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (!(newValue instanceof LazyBoxFolderTreeItem)) {
				selectedProperty.setValue(null);
				return;
			}
			LazyBoxFolderTreeItem folderItem = (LazyBoxFolderTreeItem)newValue;
			ReadOnlyBoxNavigation navigation = folderItem.getNavigation();
			if (!(navigation instanceof CachedBoxNavigation)) {
				selectedProperty.setValue(null);
				return;
			}
			Path result = ((CachedBoxNavigation) navigation).getPath();
			selectedProperty.setValue(result);
		});

		okButton.disableProperty().bind(selectedProperty.isNull());

		getDialogPane().setPrefHeight(300);
		getDialogPane().setPrefWidth(300);
		setResizable(true);
		root.setExpanded(true);

		setResultConverter(buttonType -> {
			if (buttonType != okType) {
				return null;
			}
			return selectedProperty.getValue();
		});
	}
}
