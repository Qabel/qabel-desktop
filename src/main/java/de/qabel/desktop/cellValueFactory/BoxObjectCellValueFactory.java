package de.qabel.desktop.cellValueFactory;

import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
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

        if (p.getValue().getValue() instanceof BoxFile) {
            BoxFile bf = (BoxFile) p.getValue().getValue();
            switch (searchValue) {
                case SIZE:
                    return new ReadOnlyStringWrapper(bf.size.toString());
                case MTIME:
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    return new ReadOnlyStringWrapper(dateFormat.format(bf.mtime));
                case NAME:
                    return new ReadOnlyStringWrapper(bf.name);
            }
        }
        if (p.getValue().getValue() instanceof BoxFolder) {
            BoxObject bf = p.getValue().getValue();
            if (searchValue.equals(NAME)) {
                return new ReadOnlyStringWrapper(bf.name);
            }
        }
        return new ReadOnlyStringWrapper("");
    }
}
