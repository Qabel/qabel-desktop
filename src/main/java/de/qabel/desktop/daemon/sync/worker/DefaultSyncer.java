package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.BoxNavigation;
import de.qabel.box.storage.DirectoryMetadata;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.blacklist.Blacklist;
import de.qabel.desktop.daemon.sync.blacklist.PatternBlacklist;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.LocalDeleteEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static de.qabel.desktop.daemon.management.Transaction.TYPE.DELETE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UPDATE;

public class DefaultSyncer implements Syncer {
    private static final Pattern DEFAULT_BLACKLIST_PATTERN = Pattern.compile("\\..*~");
    private ExecutorService executor;
    private static final ExecutorService fileExecutor = Executors.newSingleThreadExecutor();
    private BoxSyncBasedUploadFactory uploadFactory = new BoxSyncBasedUploadFactory();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private CachedBoxVolume boxVolume;
    private BoxSyncConfig config;
    private TransferManager manager;
    private int pollInterval = 1;
    private TimeUnit pollUnit = TimeUnit.MINUTES;
    private Thread poller;
    private TreeWatcher watcher;
    private boolean polling;
    private SyncIndex index;
    private WindowedTransactionGroup progress = new WindowedTransactionGroup();
    private List<Transaction> history = new LinkedList<>();
    private Blacklist localBlacklist;
    private Observer remoteChangeHandler;
    private CachedBoxNavigation nav;

    public DefaultSyncer(BoxSyncConfig config, CachedBoxVolume boxVolume, TransferManager manager) {
        this.config = config;
        this.boxVolume = boxVolume;
        this.manager = manager;
        config.setSyncer(this);
        PatternBlacklist defaultBlacklist = new PatternBlacklist();
        defaultBlacklist.add(DEFAULT_BLACKLIST_PATTERN);
        localBlacklist = defaultBlacklist;
    }

    public void setLocalBlacklist(Blacklist blacklist) {
        localBlacklist = blacklist;
    }

    @Override
    public List<Transaction> getHistory() {
        return history;
    }

    @Override
    public void run() {
        try {
            executor = Executors.newFixedThreadPool(10);
            ensureLocalDir();
            index = config.getSyncIndex();

            startWatcher();
            registerRemoteChangeHandler();
            registerRemotePoller();

            boxVolume.navigate().notifyAllContents();
        } catch (QblStorageException | IOException e) {
            logger.error("failed to start sync: " + e.getMessage(), e);
        }
    }

    protected void registerRemotePoller() throws QblStorageException {
        CachedBoxNavigation nav = navigateToRemoteDir();

        poller = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    try {
                        nav.refresh();
                        polling = true;
                    } catch (QblStorageException e) {
                        logger.warn("polling failed: " + e.getMessage(), e);
                    }
                    Thread.sleep(pollUnit.toMillis(pollInterval));
                }
            } catch (InterruptedException e) {
                logger.debug("poller stopped");
            } finally {
                polling = false;
            }
        });
        poller.setName("DefaultSyncer-" + config.getName() + "#Poller");
        poller.setDaemon(true);
        poller.start();
    }

    protected void registerRemoteChangeHandler() throws QblStorageException {
        nav = navigateToRemoteDir();

        remoteChangeHandler = (o, arg) -> executor.submit(() -> {
            try {
                if (!(arg instanceof ChangeEvent)) {
                    return;
                }
                String type = ((ChangeEvent) arg).getType().toString();
                logger.trace("remote update " + type + " " + ((ChangeEvent) arg).getPath());
                download((ChangeEvent) arg);
            } catch (Exception e) {
                logger.error("failed to handle remote change: " + e.getMessage(), e);
            }
        });
        nav.addObserver(remoteChangeHandler);
    }

    private CachedBoxNavigation navigateToRemoteDir() throws QblStorageException {
        Path remotePath = config.getRemotePath();
        CachedBoxNavigation nav = boxVolume.navigate();

        for (int i = 0; i < remotePath.getNameCount(); i++) {
            String name = remotePath.getName(i).toString();
            if (!nav.hasFolder(name)) {
                index.clear();
                nav.createFolder(name);
            }
            nav = nav.navigate(name);
        }
        return nav;
    }

    private void download(ChangeEvent event) {
        BoxSyncBasedDownload download = new BoxSyncBasedDownload(boxVolume, config, event);

        if (!download.getDestination().normalize().startsWith(config.getLocalPath().normalize())) {
            logger.warn("syncer received event from outside sync path: " + download);
            return;
        }

        SyncIndexEntry entry = index.get(BoxFileSystem.getRoot().relativize(download.getSource()));
        long size = download.isDir() ? DirectoryMetadata.Companion.getDEFAULT_SIZE() : download.getSize();
        Long mtime = download.isDir() ? null : download.getMtime();
        SyncState targetState = new SyncState(download.getType() != DELETE, mtime, size);

        entry.setRemoteState(targetState);
        if (targetState.equals(entry.getLocalState())) {
            logger.trace("download matches local state, dropping " + download);
            return;
        }
        if (entry.getSyncedState().equals(targetState)) {
            logger.trace("download matches synced state, checking for unnoticed delete");
            uploadUnnoticedDelete(download);
            return;
        }

        download.onSuccess(() -> entry.setSyncedState(targetState));
        download.onSkipped(() -> {
            if (download.isValid()) {
                entry.setSyncedState(targetState);
            }
        });
        addDownload(download);
    }

    private void addDownload(BoxSyncBasedDownload download) {
        progress.add(download);
        history.add(download);
        manager.addDownload(download);
    }

    private void uploadUnnoticedDelete(BoxSyncBasedDownload download) {
        if (download.getType() == DELETE) {
            return;
        }
        if (Files.exists(download.getDestination())) {
            return;
        }
        ChangeEvent inverseEvent = new LocalDeleteEvent(
                download.getDestination(),
                download.isDir(),
                System.currentTimeMillis(),
                ChangeEvent.TYPE.DELETE
        );
        upload(inverseEvent);
    }

    private void upload(WatchEvent event) {
        BoxSyncBasedUpload upload = uploadFactory.getUpload(boxVolume, config, event);

        if (localBlacklist != null) {
            if (localBlacklist.matches(upload.getDestination())) {
                return;
            }
        }
        SyncState targetState;
        targetState = new SyncState(
            upload.getType() != DELETE,
            upload.isDir() ? null : upload.getMtime(),
            upload.isDir() ? DirectoryMetadata.Companion.getDEFAULT_SIZE() : upload.getSize()
        );
        BoxPath targetPath = config.getRemotePath().resolve(config.getLocalPath().relativize(upload.getSource()));
        targetPath = BoxFileSystem.getRoot().relativize(targetPath);
        SyncIndexEntry entry = index.get(targetPath);

        entry.setLocalState(targetState);
        if (targetState.equals(entry.getRemoteState())) {
            logger.trace("upload matches remote state, dropping " + upload);
            return;
        }
        if (targetState.equals(entry.getSyncedState())) {
            logger.trace("upload matches synced state, dropping " + upload);
            downloadUnnoticedDelete(upload);
            return;
        }
        if (event.isDir() && event instanceof ChangeEvent && ((ChangeEvent)event).getType() == UPDATE) {
            return;
        }

        upload.onSuccess(() -> entry.setSyncedState(targetState));
        upload.onSkipped(() -> {
            if (upload.isValid()) {
                entry.setSyncedState(targetState);
            }
        });
        addUpload(upload);
    }

    private void addUpload(BoxSyncBasedUpload upload) {
        progress.add(upload);
        history.add(upload);
        manager.addUpload(upload);
    }

    private void downloadUnnoticedDelete(BoxSyncBasedUpload upload) {
        if (upload.getType() == DELETE) {
            return;
        }
        Path destination = upload.getDestination();
        boolean exists;
        BoxNavigation nav = null;
        try {
            nav = upload.getBoxVolume().navigate();
            for (int i = 0; i < destination.getNameCount() - 1; i++) {
                nav = nav.navigate(destination.getName(i).toString());
            }
            String filename = destination.getFileName().toString();
            exists = upload.isDir() ? nav.hasFolder(filename) : nav.hasFile(filename);
        } catch (QblStorageException | IllegalArgumentException e) {
            logger.warn(e.getMessage(), e);
            exists = false;
        }
        if (!exists) {
            ChangeEvent inverseEvent = new RemoteChangeEvent(
                    upload.getDestination(),
                    upload.isDir(),
                    System.currentTimeMillis(),
                    ChangeEvent.TYPE.DELETE,
                    null, nav
            );
            download(inverseEvent);
        }
    }

    private int localEvents;

    public boolean isProcessingLocalEvents() {
        return localEvents > 0;
    }

    protected void startWatcher() throws IOException {
        Path localPath = ensureLocalDir();
        watcher = new TreeWatcher(localPath, watchEvent -> {
            try {
                synchronized (this) {
                    localEvents++;
                }
                if (!watchEvent.isValid()) { // documentation is vague when this happens and what it means
                    return;
                }
                String type = "";
                if (watchEvent instanceof ChangeEvent)
                    type = ((ChangeEvent) watchEvent).getType().toString();
                logger.trace("local update " + type + " " + watchEvent.getPath());
                upload(watchEvent);
            } finally {
                synchronized (this) {
                    localEvents--;
                }
            }
        });
        watcher.setName("DefaultSyncer-" + config.getName() + "#Watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private Path ensureLocalDir() throws IOException {
        Path localPath = config.getLocalPath();
        if (!Files.isDirectory(localPath)) {
            Files.createDirectories(localPath);
        }
        if (!Files.isReadable(localPath)) {
            throw new IllegalStateException("local dir " + localPath + " is not valid");
        }
        return localPath;
    }

    @Override
    public void shutdown() {
        interrupt();
        progress.cancel();
    }

    public void join() throws InterruptedException {
        logger.trace("waiting for watcher");
        watcher.join();
        logger.trace("waiting for poller");
        poller.join();
        logger.trace("waiting for background tasks of sync");
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Override
    public void setPollInterval(int amount, TimeUnit unit) {
        pollInterval = amount;
        pollUnit = unit;
    }

    @Override
    public void stop() throws InterruptedException {
        interrupt();
        join();
        logger.trace("cancelling transfers");
        progress.cancel();
    }

    private void interrupt() {
        logger.trace("stopping syncer for " + config.getName());
        if (nav != null) {
            nav.deleteObserver(remoteChangeHandler);
        }
        if (watcher != null && watcher.isAlive()) {
            watcher.interrupt();
        }
        if (poller != null && poller.isAlive()) {
            poller.interrupt();
        }
        executor.shutdown();
    }

    public boolean isPolling() {
        return polling;
    }

    public void waitFor() {
        while (!polling && !watcher.isWatching()) {
            Thread.yield();
        }
    }

    public BoxSyncBasedUploadFactory getUploadFactory() {
        return uploadFactory;
    }

    public void setUploadFactory(BoxSyncBasedUploadFactory uploadFactory) {
        this.uploadFactory = uploadFactory;
    }

    @Override
    public boolean isSynced() {
        return progress.isEmpty() && isPolling() && watcher.isWatching() && poller.isAlive() && watcher.isAlive();
    }

    @Override
    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public DefaultSyncer onProgress(Runnable runnable) {
        progress.onProgress(runnable);
        return this;
    }

    @Override
    public long totalSize() {
        return progress.totalSize();
    }

    @Override
    public long currentSize() {
        return progress.currentSize();
    }

    @Override
    public int countFiles() {
        return 0;
    }

    @Override
    public int countFolders() {
        return 0;
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public DefaultSyncer onProgress(Consumer<Transaction> consumer) {
        progress.onProgress(consumer);
        return this;
    }

    @Override
    public long totalElements() {
        return progress.totalElements();
    }

    @Override
    public long finishedElements() {
        return progress.finishedElements();
    }
}
