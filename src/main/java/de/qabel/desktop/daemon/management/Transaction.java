package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public interface Transaction {
	TYPE getType();

	BoxVolume getBoxVolume();

	Path getSource();

	Path getDestination();

	boolean isValid();

	enum TYPE { CREATE, UPDATE, DELETE }
}
