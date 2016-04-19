package de.qabel.desktop.ui.actionlog;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class FxActionlog {
    private final Actionlog actionlog;
    private IntegerProperty unseenMessageCount = new SimpleIntegerProperty(0);
    private List<Consumer<PersistenceDropMessage>> newMessageHandler = new CopyOnWriteArrayList<>();

    public FxActionlog(Actionlog actionlog) {
        this.actionlog = actionlog;

        actionlog.addObserver(this::update);
    }

    public void onNewMessage(Consumer<PersistenceDropMessage> message) {
        newMessageHandler.add(message);
    }

    private void update(PersistenceDropMessage message) {
        Platform.runLater(() -> {
            unseenMessageCount.setValue(actionlog.getUnseenMessageCount());
            newMessageHandler.forEach(h -> h.accept(message));
        });
    }

    public IntegerProperty unseenMessageCountProperty() {
        return unseenMessageCount;
    }

    public StringBinding unseenMessageCountAsStringProperty() {
        return unseenMessageCount.asString();
    }
}
