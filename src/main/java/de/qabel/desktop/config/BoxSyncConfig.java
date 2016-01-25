package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;

import java.nio.file.Path;
import java.util.Observer;

public interface BoxSyncConfig {
	String getName();
	Identity getIdentity();
	Account getAccount();
	Path getLocalPath();
	Path getRemotePath();

	void setName(String name);
	void setLocalPath(Path localPath);
	void setRemotePath(Path remotePath);
	void pause();
	void unpause();
	boolean isPaused();
	void addObserver(Observer o);
	SyncIndex getSyncIndex();
}
