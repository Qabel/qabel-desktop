package de.qabel.desktop.inject.config;

import javafx.stage.Stage;

import java.net.URI;
import java.nio.file.Path;

public interface RuntimeConfiguration {
    URI getDropUri();
    Path getPersistenceDatabaseFile();
    Stage getPrimaryStage();
}
