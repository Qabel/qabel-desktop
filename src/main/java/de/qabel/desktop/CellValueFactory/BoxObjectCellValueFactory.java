package de.qabel.desktop.CellValueFactory;

import de.qabel.desktop.storage.BoxObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;



public class BoxObjectCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<BoxObject, String>, ObservableValue<String>> {

    private String searchValue;

    public BoxObjectCellValueFactory(String searchValue) {
        this.searchValue = searchValue;
    }

    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<BoxObject, String> p) {
        BoxObject bf = p.getValue().getValue();
        switch (searchValue){
            case "name":
                return new ReadOnlyStringWrapper(bf.name);
        }
        return new ReadOnlyStringWrapper("");
    }
}
