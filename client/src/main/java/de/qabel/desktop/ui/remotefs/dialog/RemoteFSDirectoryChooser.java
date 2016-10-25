package de.qabel.desktop.ui.remotefs.dialog;

import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.ReadableBoxNavigation;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.ui.remotefs.FolderTreeItem;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

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
        Path result = BoxFileSystem.pathFromBoxDto(navigation.getPath());
        selectedProperty.setValue(result);
    }
}
