package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Files;
import java.nio.file.Path;

public class ManualUpload extends AbstractManualTransaction implements Upload {

	public ManualUpload(TYPE type, BoxVolume volume, Path source, Path destination) {
		this(type, volume, source, destination, Files.isDirectory(source));
	}

	public ManualUpload(TYPE type, BoxVolume volume, Path source, Path destination, boolean isDir) {
		super(System.currentTimeMillis(), isDir, destination, source, type, volume);

	}
}
