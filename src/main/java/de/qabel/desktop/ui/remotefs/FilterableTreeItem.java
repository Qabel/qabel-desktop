package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxObject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.util.Observable;
import java.util.Observer;

public class FilterableTreeItem extends TreeItem<BoxObject> implements Filterable, Observer {
    private BooleanProperty visibleProperty = new SimpleBooleanProperty(true);
    private StringProperty filterProperty = new SimpleStringProperty("");

    public FilterableTreeItem(BoxObject value) {
        super(value);
        initialize();
    }

    public FilterableTreeItem(BoxObject value, ImageView imageView) {
        super(value, imageView);
        initialize();
    }

    private void initialize() {
        visibleProperty.bind(filterProperty.isEmpty().or(containsIgnoreCase(filterProperty)));
        getValue().addObserver(this);
    }

    private BooleanBinding containsIgnoreCase(StringProperty filterProperty) {
        return new StringContainmentBinding(filterProperty, this::getValue);
    }

    @Override
    public BooleanProperty visibleProperty() {
        return visibleProperty;
    }

    @Override
    public StringProperty filterProperty() {
        return filterProperty;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != getValue()) {
            return;
        }

        Event.fireEvent(this, new TreeModificationEvent<>(valueChangedEvent(), this));
    }
}
