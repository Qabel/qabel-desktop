package de.qabel.desktop.inject.config;

import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.repository.sqlite.ClientDatabase;
import de.qabel.desktop.ui.AbstractController;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.InputStream;
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
    private String thanksFileContent;

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

    public void loadAboutFiles() {
        loadThanksFile("/files/thanks_file");
    }

    private void loadThanksFile(String thanksFilePath) {
        try {
            thanksFileContent = readFile(thanksFilePath);
        } catch (IOException e) {
            alert("failed to load thanks file", e);
        } catch (NullPointerException ignored) {
        }
    }

    private String readFile(String filePath) throws IOException {
        try (InputStream thanksFile = System.class.getResourceAsStream(filePath)) {
            return IOUtils.toString(thanksFile, "UTF-8");
        } catch (NullPointerException ignored) {
        }
        return Strings.EMPTY;
    }

    public String getThanksFileContent() {
        return thanksFileContent;
    }
}
