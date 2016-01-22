package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public interface Transaction {
	TYPE getType();

	BoxVolume getBoxVolume();

	Path getSource();

	Path getDestination();

	boolean isValid();

	boolean isDir();

	Long getMtime();

	enum TYPE { CREATE, UPDATE, DELETE }
	enum STATE { INITIALIZING, SCHEDULED, RUNNING, FINISHED, FAILED, SKIPPED }

	STATE getState();

	void toState(STATE state);
}
