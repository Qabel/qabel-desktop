package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.storage.*;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Collections;
import java.util.LinkedList;

public class FilterableFolderTreeItem extends FolderTreeItem {
    private static final Image fileImg = new Image(FilterableFolderTreeItem.class.getResourceAsStream("/icon/file.png"),  18, 18, true, false);
    private final StringProperty filterProperty = new SimpleStringProperty(null);
    private final ObservableList<TreeItem<BoxObject>> filteredChildren = FXCollections.observableList(Collections.synchronizedList(new LinkedList<>()));
    private BooleanProperty hasVisibleChildren = new SimpleBooleanProperty(false);
    private BooleanProperty visibleProperty = new SimpleBooleanProperty(true);
    private Boolean expandedBeforeFilter;

    public FilterableFolderTreeItem(BoxFolder folder, ReadableBoxNavigation navigation) {
        super(folder, navigation);
        initialize();
    }

    public FilterableFolderTreeItem(BoxFolder folder, ReadableBoxNavigation navigation, Image icon) {
        super(folder, navigation, icon);
        initialize();
    }

    private void initialize() {
        super.getChildren();
        filteredChildren.addListener((ListChangeListener<? super TreeItem<BoxObject>>) c1 -> {
            hasVisibleChildren.setValue(!filteredChildren.isEmpty());
            Event.fireEvent(this, new TreeModificationEvent<>(childrenModificationEvent(), this));
        });
        visibleProperty.bind(filterProperty.isEmpty().or(containsIgnoreCase(filterProperty).or(hasVisibleChildren)));
        visibleProperty.addListener(observable1 -> {
            Event.fireEvent(this, new TreeModificationEvent<>(valueChangedEvent(), this));
        });

        filterProperty.addListener((observable, oldValue, newValue) -> {
            if (expandedBeforeFilter == null) {
                expandedBeforeFilter = isExpanded();
                expandedProperty().bind(visibleProperty);
            }
            if (filterProperty.isEmpty().get()) {
                expandedProperty().unbind();
            }
            if (filterProperty.isEmpty().get() && expandedBeforeFilter != null) {
                setExpanded(expandedBeforeFilter);
                expandedBeforeFilter = null;
            }
        });

        ObservableList<TreeItem<BoxObject>> children = super.getChildren();
        children.addListener((ListChangeListener<? super TreeItem<BoxObject>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (TreeItem<BoxObject> item : c.getAddedSubList()) {
                        addChild(item);
                    }
                }
                if (c.wasRemoved()) {
                    for (TreeItem<BoxObject> item : c.getRemoved()) {
                        removeChild(item);
                    }
                }
            }
        });
    }

    private void removeChild(TreeItem<BoxObject> item) {
        Platform.runLater(() -> filteredChildren.remove(item));
    }


    private void addChild(TreeItem<BoxObject> item) {
        Platform.runLater(() -> filteredChildren.add(item));
    }

    private BooleanBinding containsIgnoreCase(StringProperty filterProperty) {
        return new StringContainmentBinding(filterProperty, this::getValue);
    }

    public StringProperty filterProperty() {
        return filterProperty;
    }

    @Override
    protected FolderTreeItem initSubFolderItem(BoxFolder folder, BoxNavigation subNavigation) {
        final FilterableFolderTreeItem item = new FilterableFolderTreeItem(folder, subNavigation);
        item.visibleProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue == newValue) {
                return;
            }

            synchronized (filteredChildren) {
                if (newValue) {
                    if (super.getChildren().contains(item)) {
                        addChild(item);
                    }
                } else {
                    removeChild(item);
                }
            }
        });
        item.filterProperty().bind(filterProperty);
        return item;
    }

    @Override
    protected FilterableTreeItem initSubFileItem(BoxFile file) {
        final FilterableTreeItem item = new FilterableTreeItem(file, new ImageView(fileImg));
        item.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == newValue) {
                return;
            }

            synchronized (filteredChildren) {
                if (newValue) {
                    addChild(item);
                } else {
                    removeChild(item);
                }
            }
        });
        item.filterProperty().bind(filterProperty);
        return item;
    }

    @Override
    public ObservableList<TreeItem<BoxObject>> getChildren() {
        ObservableList<TreeItem<BoxObject>> children = super.getChildren();
        if (filterProperty.isEmpty().get()) {
            return children;
        }
        return filteredChildren;
    }
}
