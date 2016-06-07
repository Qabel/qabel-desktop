package de.qabel.desktop.config;

import com.google.common.io.Files;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;
import java.util.Observer;
import java.util.function.Consumer;

public class StubBoxSyncConfig implements BoxSyncConfig {
    public int id;
    public String name;
    public Identity identity;
    public Account account;
    public Path localPath = Files.createTempDir().toPath();
    public BoxPath remotePath;
    public boolean paused = false;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public BoxPath getRemotePath() {
        return remotePath;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLocalPath(Path localPath) {
        this.localPath = localPath;
    }

    @Override
    public void setRemotePath(BoxPath remotePath) {
        this.remotePath = remotePath;
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void unpause() {
        paused = false;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void addObserver(Observer o) {

    }

    @Override
    public SyncIndex getSyncIndex() {
        return null;
    }

    @Override
    public void setSyncer(Syncer syncer) {

    }

    @Override
    public Syncer getSyncer() {
        return null;
    }

    @Override
    public void withSyncer(Consumer<Syncer> callback) {

    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
