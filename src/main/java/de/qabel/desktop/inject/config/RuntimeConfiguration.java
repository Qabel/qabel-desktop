package de.qabel.desktop.inject.config;

import de.qabel.desktop.repository.sqlite.ClientDatabase;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URI;

public interface RuntimeConfiguration {
    URI getDropUri();
    URI getAccountingUri();
    URI getBlockUri();
    Stage getPrimaryStage();
    Pane getWindow();
    String getThanksFileContent();
    ClientDatabase getConfigDatabase();
}
