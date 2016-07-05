package de.qabel.desktop.inject.config;

import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StaticRuntimeConfiguration extends AbstractController implements RuntimeConfiguration {
    private URI dropUri;
    private URI accountingUri;
    private URI blockUri;
    private Stage primaryStage;
    private ClientDatabase configDatabase;
    private Pane window;
    private FilesAbout filesAbout;
    private String currentVersion;

    public StaticRuntimeConfiguration(
        LaunchConfig launchConfig,
        ClientDatabase configDatabase
    ) throws URISyntaxException, IOException {
        dropUri = launchConfig.getDropUrl().toURI();
        accountingUri = launchConfig.getAccountingUrl().toURI();
        blockUri = launchConfig.getBlockUrl().toURI();
        this.configDatabase = configDatabase;
        filesAbout = new FilesAbout();
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
