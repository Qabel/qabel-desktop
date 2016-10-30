package de.qabel.desktop.event;

import de.qabel.core.event.Event;
import javafx.stage.Stage;

public class ClientStartedEvent implements Event {
    private Stage primaryStage;
    public ClientStartedEvent(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
