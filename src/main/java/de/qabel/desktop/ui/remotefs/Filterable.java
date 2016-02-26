package de.qabel.desktop.ui.remotefs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

public interface Filterable {
	BooleanProperty visibleProperty();
	StringProperty filterProperty();
}
