package de.qabel.desktop.CellValueFactory;

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

    private String searchValue;

    public BoxObjectCellValueFactory(String searchValue) {
        this.searchValue = searchValue;
    }

    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BoxObject, String> p) {

        if (!(p.getValue().getValue() instanceof BoxObject)) {
            return new ReadOnlyStringWrapper("");
        }

        if (p.getValue().getValue() instanceof BoxFile) {
            BoxFile bf = (BoxFile) p.getValue().getValue();
            switch (searchValue) {
                case "size":
                    return new ReadOnlyStringWrapper(bf.size.toString());
                case "mtime":
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    return new ReadOnlyStringWrapper(dateFormat.format(bf.mtime));
                case "name":
                    return new ReadOnlyStringWrapper(bf.name);
            }
        }
        if (p.getValue().getValue() instanceof BoxFolder) {
            BoxObject bf = p.getValue().getValue();
            if (searchValue == "name") {
                return new ReadOnlyStringWrapper(bf.name);
            } else {
                return new ReadOnlyStringWrapper("");
            }
        }
        return new ReadOnlyStringWrapper("");
    }
}
