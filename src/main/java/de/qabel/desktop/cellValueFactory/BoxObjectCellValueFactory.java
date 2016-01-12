package de.qabel.desktop.cellValueFactory;

import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxObject;
import de.qabel.desktop.ui.remotefs.LazyBoxFolderTreeItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class BoxObjectCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<BoxObject, String>, ObservableValue<String>> {
	public static final String SIZE = "size";
	public static final String MTIME = "mtime";
	public static final String NAME = "name";

	private String searchValue;

	public BoxObjectCellValueFactory(String searchValue) {
		this.searchValue = searchValue;
	}

	public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BoxObject, String> p) {
		TreeItem<BoxObject> treeItem = p.getValue();
		BoxObject bf = treeItem.getValue();

		if (searchValue.equals(NAME)) {
			if (treeItem instanceof LazyBoxFolderTreeItem) {
				return ((LazyBoxFolderTreeItem) treeItem).getNameProperty();
			}
			return new ReadOnlyStringWrapper(bf.name);
		}

		if (bf instanceof BoxFile) {
			switch (searchValue) {
				case SIZE:
					return new ReadOnlyStringWrapper(((BoxFile) bf).size.toString());
				case MTIME:
					DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					return new ReadOnlyStringWrapper(dateFormat.format(((BoxFile) bf).mtime));
			}
		}

		return new ReadOnlyStringWrapper("");
	}
}
