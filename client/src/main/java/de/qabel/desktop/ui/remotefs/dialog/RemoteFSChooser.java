package de.qabel.desktop.ui.remotefs.dialog;

import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxNavigation;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.ui.remotefs.BoxObjectTreeCell;
import de.qabel.desktop.ui.remotefs.FolderTreeItem;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.ResourceBundle;

public abstract class RemoteFSChooser extends Dialog<Path> implements ChangeListener<TreeItem<BoxObject>> {
    private final TreeView<BoxObject> tree;
    private final TreeItem<BoxObject> root;
    ObjectPropertyBase<Path> selectedProperty = new SimpleObjectProperty<>(null);
    private Button okButton;

    RemoteFSChooser(ResourceBundle resources, BoxVolume volume) throws QblStorageException {

        setTitle(resources.getString("syncSetupChooseRemoteFolder"));
        ButtonType okType = new ButtonType(resources.getString("open"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(resources.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(okType);
        getDialogPane().getButtonTypes().add(cancelType);

        okButton = (Button) getDialogPane().lookupButton(okType);

        BoxNavigation nav = volume.navigate();
        root = new FolderTreeItem(new BoxFolder("don't care", "/", new byte[16]), nav);
        tree = new TreeView<>(root);
        getDialogPane().setContent(tree);

        tree.setCellFactory(param -> new BoxObjectTreeCell());
        tree.getSelectionModel().selectedItemProperty().addListener(this);

        getDialogPane().setPrefHeight(300);
        getDialogPane().setPrefWidth(300);
        setResizable(true);
        root.setExpanded(true);

        okButton.disableProperty().bind(selectedProperty.isNull());
        setResultConverter(buttonType -> {
            if (buttonType != okType) {
                return null;
            }
            return selectedProperty.getValue();
        });
    }
}
