package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FXBoxSyncConfig {
	private StringProperty nameProperty;
	private StringProperty localPathProperty;
	private StringProperty remotePathProperty;

	public FXBoxSyncConfig(BoxSyncConfig config) {
		nameProperty = new SimpleStringProperty(config.getName());
		localPathProperty = new SimpleStringProperty(config.getLocalPath().toString());
		remotePathProperty = new SimpleStringProperty(config.getRemotePath().toString());

		config.addObserver((o, arg) -> {
			nameProperty.set(config.getName());
			localPathProperty.set(config.getLocalPath().toString());
			remotePathProperty.set(config.getRemotePath().toString());
		});
	}

	public StringProperty nameProperty() {
		return nameProperty;
	}

	public StringProperty localPathProperty() {
		return localPathProperty;
	}

	public StringProperty remotePathProperty() {
		return remotePathProperty;
	}
}
