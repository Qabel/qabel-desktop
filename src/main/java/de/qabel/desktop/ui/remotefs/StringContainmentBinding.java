package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.storage.BoxObject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;

import java.util.concurrent.Callable;

public class StringContainmentBinding extends BooleanBinding {
	private final StringProperty filterProperty;
	private final Callable<BoxObject> valueProvider;

	public StringContainmentBinding(StringProperty filterProperty, Callable<BoxObject> valueProvider) {
        this.filterProperty = filterProperty;
		this.valueProvider = valueProvider;
		super.bind(filterProperty);
	}

	@Override
	public void dispose() {
		super.dispose();
		super.unbind(filterProperty);
	}

	@Override
	protected boolean computeValue() {
		BoxObject value;
		try {
			value = valueProvider.call();
		} catch (Exception e) {
			return false;
		}
		if (value == null) {
			return false;
		}
		if (value.getName() == null) {
			return false;
		}
		String filterValue = filterProperty.get();
		if (filterValue == null) {
			return false;
		}
		return value.getName().toLowerCase().contains(filterValue.toLowerCase());
	}
}
