package de.qabel.desktop.ui;

import de.qabel.desktop.daemon.NetworkStatus;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Observable;
import java.util.Observer;

public class FxNetworkStatus implements Observer {
    private NetworkStatus status;
    private BooleanProperty onlineProperty;

    public FxNetworkStatus(NetworkStatus status) {
        this.status = status;
        onlineProperty = new SimpleBooleanProperty(status.isOnline());
        status.addObserver(this);
    }

    public BooleanProperty onlineProperty() {
        return onlineProperty;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != status) {
            return;
        }

        Platform.runLater(() -> onlineProperty.setValue(status.isOnline()));
    }
}
