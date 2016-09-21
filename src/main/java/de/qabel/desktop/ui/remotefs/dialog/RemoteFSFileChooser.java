package de.qabel.desktop.ui.remotefs.dialog;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.ReadableBoxNavigation;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.storage.PathNavigation;
import de.qabel.desktop.ui.remotefs.FolderTreeItem;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.util.ResourceBundle;

public class RemoteFSFileChooser extends RemoteFSChooser {
    public RemoteFSFileChooser(ResourceBundle resources, BoxVolume volume) throws QblStorageException {
        super(resources, volume);
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<BoxObject>> observable, TreeItem<BoxObject> oldValue, TreeItem<BoxObject> newValue) {
        if (!(newValue.getValue() instanceof BoxFile)) {
            selectedProperty.setValue(null);
            return;
        }
        if (!(newValue.getParent() instanceof FolderTreeItem)) {
            return;
        }

        FolderTreeItem folderItem = (FolderTreeItem)newValue.getParent();
        ReadableBoxNavigation navigation = folderItem.getNavigation();
        if (!(navigation instanceof PathNavigation)) {
            selectedProperty.setValue(null);
            return;
        }
        Path result = ((PathNavigation) navigation).getDesktopPath().resolve(newValue.getValue().getName());
        selectedProperty.setValue(result);
    }
}
