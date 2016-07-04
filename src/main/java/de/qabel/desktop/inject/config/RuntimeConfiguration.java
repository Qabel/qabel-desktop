package de.qabel.desktop.inject.config;

import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.desktop.config.FilesAbout;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URI;
import java.nio.file.Path;

public interface RuntimeConfiguration {
    URI getDropUri();
    URI getAccountingUri();
    URI getBlockUri();
    Path getPersistenceDatabaseFile();
    Stage getPrimaryStage();
    Pane getWindow();
    FilesAbout getAboutFilesContent();
    ClientDatabase getConfigDatabase();
    String getCurrentVersion();
}
