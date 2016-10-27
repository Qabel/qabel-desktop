package de.qabel.desktop.ui.actionlog;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

public class Hyperlink extends Text {
    public Hyperlink(String text) {
        super(text);
        getStyleClass().add("hyperlink");

        setOnMouseClicked(this::fireActionEvent);
        setOnKeyTyped(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
               fireActionEvent(keyEvent);
            }
        });
    }

    protected void fireActionEvent(Event mouseEvent) {
        onActionProperty().get().handle(new ActionEvent(
            mouseEvent.getSource(), mouseEvent.getTarget()
        ));
    }

    private ObjectProperty<EventHandler<ActionEvent>> onAction;
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (onAction == null) {
            onAction = new SimpleObjectProperty<>(this, "onAction", event -> {});
        }
        return onAction;
    }

    public final void setOnAction(EventHandler<ActionEvent> handler) {
        onActionProperty().setValue(handler);
    }
}
