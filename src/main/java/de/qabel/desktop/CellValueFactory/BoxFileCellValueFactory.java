package de.qabel.desktop.CellValueFactory;

import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class BoxFileCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<BoxObject, String>, ObservableValue<String>> {

    private String searchValue;

    public BoxFileCellValueFactory(String searchValue) {
        this.searchValue = searchValue;
    }

    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BoxObject, String> p) {

        if (!(p.getValue().getValue() instanceof BoxFile)) {
            return new ReadOnlyStringWrapper("");
        }

            BoxFile bf = (BoxFile) p.getValue().getValue();

            switch (searchValue){
                case "size":
                    return new ReadOnlyStringWrapper(bf.size.toString());
                case "mtime":
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    return new ReadOnlyStringWrapper(dateFormat.format(bf.mtime));
            }

        return new ReadOnlyStringWrapper("");
    }
}
