package de.qabel.desktop.inject.config;

import de.qabel.desktop.repository.sqlite.ClientDatabase;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URI;
import java.nio.file.Path;

public interface RuntimeConfiguration {
    URI getDropUri();
    Path getPersistenceDatabaseFile();
    Stage getPrimaryStage();
    Pane getWindow();
    ClientDatabase getConfigDatabase();
}
