package de.qabel.desktop.ui.remotefs.dialog;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.remotefs.FolderTreeItem;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.ResourceBundle;

public class RemoteFSDirectoryChooser extends RemoteFSChooser {

	public RemoteFSDirectoryChooser(ResourceBundle resources, BoxVolume volume) throws QblStorageException {
		super(resources, volume);
	}

	@Override
	public void changed(ObservableValue<? extends TreeItem<BoxObject>> observable, TreeItem<BoxObject> oldValue, TreeItem<BoxObject> newValue) {
		if (!(newValue instanceof FolderTreeItem)) {
			selectedProperty.setValue(null);
			return;
		}
		FolderTreeItem folderItem = (FolderTreeItem)newValue;
		ReadableBoxNavigation navigation = folderItem.getNavigation();
		if (!(navigation instanceof PathNavigation)) {
			selectedProperty.setValue(null);
			return;
		}
		Path result = ((PathNavigation) navigation).getPath();
		selectedProperty.setValue(result);
	}
}
