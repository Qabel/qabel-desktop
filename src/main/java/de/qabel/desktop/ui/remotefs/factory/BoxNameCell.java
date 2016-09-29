package de.qabel.desktop.ui.remotefs.factory;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxObject;
import de.qabel.desktop.ui.remotefs.FilterableFolderTreeItem;
import de.qabel.desktop.ui.util.Icons;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static de.qabel.desktop.ui.util.Icons.FOLDER;
import static de.qabel.desktop.ui.util.Icons.SHARE;

public class BoxNameCell extends TreeTableCell<BoxObject, String> {
    private static Image folderImg = Icons.getImage(FOLDER);
    private static final Image fileImg = Icons.getImage(FILE);
    private static final Image shareImg = Icons.getImage(SHARE);

    private BoxObject shareObject;

    private BoxObject lastObject;
    private boolean wasEmpty;
    private String lastItem;

    public BoxNameCell(BoxObject shareObject) {
        this.shareObject = shareObject;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        BoxObject boxObject = super.getTreeTableRow().getItem();
        if (empty == wasEmpty && boxObject == lastObject && item != null && item.equals(lastItem)) {
            return;
        }
        lastObject = boxObject;
        wasEmpty = empty;
        lastItem = item;

        ImageView icon = null;
        if (boxObject == shareObject) {
            icon = Icons.iconFromImage(shareImg, 18);
        } else if (boxObject instanceof BoxFile) {
            icon = Icons.iconFromImage(fileImg);
        } else if (boxObject instanceof BoxFolder) {
            icon = Icons.iconFromImage(folderImg, 18);
        }

        setText(empty ? null : item);
        setGraphic(icon);
    }
}
