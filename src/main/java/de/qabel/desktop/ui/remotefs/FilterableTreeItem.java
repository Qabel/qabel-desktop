package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.storage.BoxObject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class FilterableTreeItem extends TreeItem<BoxObject> implements Filterable {
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
}
