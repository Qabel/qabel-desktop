package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.HasId;

import java.nio.file.Path;
import java.util.Observer;
import java.util.function.Consumer;

public interface BoxSyncConfig extends HasId {
    String getName();

    Identity getIdentity();

    Account getAccount();

    Path getLocalPath();

    BoxPath getRemotePath();

    void setName(String name);

    void setLocalPath(Path localPath);

    void setRemotePath(Path remotePath);

    void pause();

    void unpause();

    boolean isPaused();

    void addObserver(Observer o);

    SyncIndex getSyncIndex();

    void setSyncer(Syncer syncer);

    Syncer getSyncer();

    void withSyncer(Consumer<Syncer> callback);
}
