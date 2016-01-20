package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public interface Upload {
	enum TYPE { CREATE, UPDATE, DELETE }

	TYPE getType();
	BoxVolume getBoxVolume();
	Path getSource();
	Path getDestination();
	boolean isValid();
}
