package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;

import java.nio.file.Path;
import java.util.Observer;

public interface BoxSyncConfig {
	void addObserver(Observer o);

	void setLocalPath(Path localPath);

	Path getLocalPath();

	Path getRemotePath();

	void setRemotePath(Path remotePath);

	void pause();

	void unpause();

	boolean isPaused();

	Identity getIdentity();

	Account getAccount();
}
