package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Consumer;

public class DefaultBoxSyncConfig extends Observable implements BoxSyncConfig, Observer {
    private static final String DEFAULT_NAME = "New Sync Config";
    private SyncIndex syncIndex = new SyncIndex();
    private int id;
    private Path localPath;
    private Path remotePath;
    private Identity identity;
    private Account account;
    private Boolean paused = false;
    private String name;
    private transient Syncer syncer;
    private final List<Consumer<Syncer>> syncerConsumers = new LinkedList<>();

    public DefaultBoxSyncConfig(Path localPath, Path remotePath, Identity identity, Account account) {
        this(DEFAULT_NAME, localPath, remotePath, identity, account);
    }

    public DefaultBoxSyncConfig(String name, Path localPath, Path remotePath, Identity identity, Account account) {
        this.name = name;
        this.identity = identity;
        this.account = account;
        setLocalPath(localPath);
        setRemotePath(remotePath);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setLocalPath(Path localPath) {
        if (!localPath.isAbsolute()) {
            throw new IllegalArgumentException("localPath must be absolute");
        }
        if (!localPath.equals(this.localPath)) {
            setChanged();
        }
        this.localPath = localPath;
        notifyObservers(localPath);
    }

    @Override
    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public Path getRemotePath() {
        return remotePath;
    }

    @Override
    public void setRemotePath(Path remotePath) {
        if (!remotePath.isAbsolute()) {
            remotePath = BoxFileSystem.getRoot().resolve(remotePath);
        }
        if (this.remotePath != null && !this.remotePath.equals(remotePath)) {
            setChanged();
        }

        this.remotePath = BoxFileSystem.get(remotePath);
        notifyObservers(remotePath);
    }

    @Override
    public void pause() {
        if (!isPaused()) {
            setChanged();
        }

        paused = true;
        notifyObservers();
    }

    @Override
    public void unpause() {
        if (isPaused()) {
            setChanged();
        }

        paused = false;
        notifyObservers();
    }

    @Override
    public boolean isPaused() {
        return paused;
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SyncIndex getSyncIndex() {
        syncIndex.addObserver(this);
        return syncIndex;
    }

    @Override
    public void setSyncer(Syncer syncer) {
        this.syncer = syncer;
        synchronized (syncerConsumers) {
            syncerConsumers.forEach(sc -> sc.accept(syncer));
        }
    }

    @Override
    public Syncer getSyncer() {
        return syncer;
    }

    @Override
    public void withSyncer(Consumer<Syncer> callback) {
        if (getSyncer() != null) {
            callback.accept(getSyncer());
        } else {
            synchronized (syncerConsumers) {
                syncerConsumers.add(callback);
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    public void setSyncIndex(SyncIndex syncIndex) {
        this.syncIndex = syncIndex;
    }
}
