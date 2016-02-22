package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxObject;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BoxObjectTreeCell extends TreeCell<BoxObject> {
	private static Image fileImg = new Image(BoxObjectTreeCell.class.getResourceAsStream("/icon/file.png"), 16, 16, true, true);
	private static Image folderImg = new Image(BoxObjectTreeCell.class.getResourceAsStream("/icon/folder.png"), 16, 16, true, true);

	public BoxObjectTreeCell() {
		super();
		itemProperty().addListener((observable, oldValue, newValue) -> {
			setText(newValue == null ? "?" : newValue.getName());
			setGraphic(new ImageView(newValue instanceof BoxFolder ? folderImg : fileImg));
		});
	}
}
