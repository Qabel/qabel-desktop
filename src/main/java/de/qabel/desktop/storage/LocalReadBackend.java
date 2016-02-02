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
		try {
			return download(name, null);
		} catch (UnmodifiedException e) {
			throw new IllegalStateException(e);
		}
	}

	public StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException {
		Path file = root.resolve(name);

		try {
			if (ifModifiedVersion != null && String.valueOf(Files.getLastModifiedTime(file).toMillis()).equals(ifModifiedVersion)) {
				throw new UnmodifiedException();
			}
		} catch (IOException e) {
			// best effort
		}

		logger.info("Downloading file path " + file.toString());
		try {
			return new StorageDownload(
					Files.newInputStream(file),
					String.valueOf(Files.getLastModifiedTime(file).toMillis()),
					Files.size(file)
			);
		} catch (IOException e) {
			throw new QblStorageNotFound(e);
		}
	}

}