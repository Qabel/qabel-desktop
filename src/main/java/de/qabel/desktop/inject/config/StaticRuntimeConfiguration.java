package de.qabel.desktop.inject.config;

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticRuntimeConfiguration implements RuntimeConfiguration {
	private URI dropUrl;
	private Path persistenceDatabaseFile;
	private Stage primaryStage;

	public StaticRuntimeConfiguration(String dropUrl, Path persistenceDatabaseFile) throws URISyntaxException, IOException {
		this.dropUrl = new URI(dropUrl);
		persistenceDatabaseFile = persistenceDatabaseFile.normalize();
		this.persistenceDatabaseFile = persistenceDatabaseFile;

		if (!Files.exists(persistenceDatabaseFile) && !Files.exists(persistenceDatabaseFile.getParent())) {
			Files.createDirectories(persistenceDatabaseFile.getParent());
		}
	}

	@Override
	public URI getDropUri() {
		return dropUrl;
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
}
