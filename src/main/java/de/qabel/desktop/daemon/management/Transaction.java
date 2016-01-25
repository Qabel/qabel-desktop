package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public interface Transaction extends AutoCloseable {
	long transactionAge();

	TYPE getType();

	BoxVolume getBoxVolume();

	Path getSource();

	Path getDestination();

	boolean isValid();

	boolean isDir();

	Long getMtime();

	Transaction onSuccess(Runnable runnable);

	Transaction onFailure(Runnable runnable);

	Transaction onSkipped(Runnable runnable);

	enum TYPE { CREATE, UPDATE, DELETE }
	enum STATE { INITIALIZING, SCHEDULED, RUNNING, FINISHED, FAILED, WAITING, SKIPPED }

	STATE getState();

	void toState(STATE state);

	@Override
	void close();
}
