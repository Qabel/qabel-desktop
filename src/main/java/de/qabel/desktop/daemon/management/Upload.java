package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolumeConfig;

import java.nio.file.Path;

public interface Upload {
	BoxVolumeConfig getBoxVolumeConfig();
	Path getSource();
	Path getDestination();
	boolean isValid();
}
