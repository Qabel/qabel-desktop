package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxObject;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class StaticTreeItemContainer extends TreeItem<BoxObject> {
    public StaticTreeItemContainer(BoxObject value, Node graphic) {
        super(value, graphic);
    }
}
