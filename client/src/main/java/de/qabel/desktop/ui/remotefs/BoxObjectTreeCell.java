package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxObject;
import de.qabel.desktop.ui.util.Icons;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;

import static de.qabel.desktop.ui.util.Icons.FOLDER;

public class BoxObjectTreeCell extends TreeCell<BoxObject> {
    private static Image fileImg = new Image(BoxObjectTreeCell.class.getResourceAsStream("/icon/file.png"), 16, 16, true, true);
    private static Image folderImg = Icons.getImage(FOLDER);

    public BoxObjectTreeCell() {
        itemProperty().addListener((observable, oldValue, newValue) -> {
            setText(newValue == null ? "?" : newValue.getName());
            setGraphic(Icons.iconFromImage(newValue instanceof BoxFolder ? folderImg : fileImg, 16));
        });
    }
}
