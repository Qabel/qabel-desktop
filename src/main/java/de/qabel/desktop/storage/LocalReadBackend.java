package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalReadBackend implements StorageReadBackend {
	private static final Logger logger = LoggerFactory.getLogger(LocalReadBackend.class.getSimpleName());
	private final Path root;

	public LocalReadBackend(Path root) {
		this.root = root;
	}

	public StorageDownload download(String name) throws QblStorageException {
		Path file = root.resolve(name);
		logger.info("Downloading file path " + file.toString());
		try {
			return new StorageDownload(
					Files.newInputStream(file),
					Files.getLastModifiedTime(file).toMillis(),
					Files.size(file)
			);
		} catch (IOException e) {
			throw new QblStorageNotFound(e);
		}
	}

}