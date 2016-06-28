package de.qabel.desktop.inject.config;

import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.repository.sqlite.ClientDatabase;
import de.qabel.desktop.ui.AbstractController;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticRuntimeConfiguration extends AbstractController implements RuntimeConfiguration {
    private URI dropUri;
    private URI accountingUri;
    private URI blockUri;
    private Path persistenceDatabaseFile;
    private Stage primaryStage;
    private ClientDatabase configDatabase;
    private Pane window;
    private FilesAbout filesAbout;
    private String currentVersion;

    public StaticRuntimeConfiguration(
        LaunchConfig launchConfig,
        Path persistenceDatabaseFile,
        ClientDatabase configDatabase
    ) throws URISyntaxException, IOException {
        dropUri = launchConfig.getDropUrl().toURI();
        accountingUri = launchConfig.getAccountingUrl().toURI();
        blockUri = launchConfig.getBlockUrl().toURI();
        persistenceDatabaseFile = persistenceDatabaseFile.normalize();
        this.persistenceDatabaseFile = persistenceDatabaseFile;
        this.configDatabase = configDatabase;
        filesAbout = new FilesAbout();

        if (!Files.exists(persistenceDatabaseFile) && !Files.exists(persistenceDatabaseFile.getParent())) {
            Files.createDirectories(persistenceDatabaseFile.getParent());
        }
    }

    @Override
    public URI getDropUri() {
        return dropUri;
    }

    @Override
    public URI getAccountingUri() {
        return accountingUri;
    }

    @Override
    public URI getBlockUri() {
        return blockUri;
    }

    @Override
    public Path getPersistenceDatabaseFile() {
        return persistenceDatabaseFile;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public ClientDatabase getConfigDatabase() {
        return configDatabase;
    }

    public void setWindow (Pane window) {
        this.window = window;
    }

    public Pane getWindow() {
        return window;
    }

    public FilesAbout getAboutFilesContent() {
        return filesAbout;
    }

    public void setCurrentVersion (String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getCurrentVersion(){
        return currentVersion;
    }
}
