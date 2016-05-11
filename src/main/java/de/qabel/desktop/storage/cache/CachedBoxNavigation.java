package de.qabel.desktop.storage.cache;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.*;

public class CachedBoxNavigation<T extends BoxNavigation> extends Observable implements BoxNavigation, PathNavigation {
    private static final Logger logger = LoggerFactory.getLogger(CachedBoxNavigation.class);
    protected final T nav;
    private final BoxNavigationCache<CachedBoxNavigation> cache = new BoxNavigationCache<>();
    private final Path path;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CachedBoxNavigation(T nav, Path path) {
        this.nav = nav;
        this.path = path;
    }

    @Override
    public DirectoryMetadata reloadMetadata() throws QblStorageException {
        return nav.reloadMetadata();
    }

    @Override
    public void setMetadata(DirectoryMetadata dm) {
        nav.setMetadata(dm);
    }

    @Override
    public void commit() throws QblStorageException {
        nav.commit();
    }

    @Override
    public void commitIfChanged() throws QblStorageException {
        nav.commitIfChanged();
    }

    @Override
    public synchronized CachedBoxNavigation navigate(BoxFolder target) throws QblStorageException {
        if (!cache.has(target)) {

            CachedBoxNavigation subnav = new CachedBoxNavigation(
                nav.navigate(target),
                    BoxFileSystem.get(path).resolve(target.getName())
            );
            cache.cache(target, subnav);
            subnav.addObserver((o, arg) -> {setChanged(); notifyObservers(arg);});
        }
        return cache.get(target);
    }

    @Override
    public BoxNavigation navigate(BoxExternal target) {
        return nav.navigate(target);
    }

    @Override
    public List<BoxFile> listFiles() throws QblStorageException {
        return nav.listFiles();
    }

    @Override
    public List<BoxFolder> listFolders() throws QblStorageException {
        return nav.listFolders();
    }

    @Override
    public List<BoxExternal> listExternals() throws QblStorageException {
        return nav.listExternals();
    }

    @Override
    public BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException {
        BoxFile upload = nav.upload(name, file, listener);
        notifyAsync(upload, CREATE);
        return upload;
    }

    @Override
    public boolean isUnmodified() {
        return nav.isUnmodified();
    }

    @Override
    public BoxFile upload(String name, File file) throws QblStorageException {
        BoxFile upload = nav.upload(name, file);
        notifyAsync(upload, CREATE);
        return upload;
    }

    protected void notifyAsync(BoxObject boxObject, TYPE type) {
        executor.submit(() -> notify(boxObject, type));
    }

    @Override
    public BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException {
        BoxFile overwrite = nav.overwrite(name, file, listener);
        notifyAsync(overwrite, UPDATE);
        return overwrite;
    }

    @Override
    public BoxFile overwrite(String name, File file) throws QblStorageException {
        BoxFile overwrite = nav.overwrite(name, file);
        notifyAsync(overwrite, UPDATE);
        return overwrite;
    }

    @Override
    public InputStream download(BoxFile file, ProgressListener listener) throws QblStorageException {
        return nav.download(file, listener);
    }

    @Override
    public InputStream download(BoxFile file) throws QblStorageException {
        return nav.download(file);
    }

    @Override
    public FileMetadata getFileMetadata(BoxFile boxFile) throws IOException, InvalidKeyException, QblStorageException {
        return nav.getFileMetadata(boxFile);
    }

    @Override
    public BoxFolder createFolder(String name) throws QblStorageException {
        BoxFolder folder = nav.createFolder(name);
        notifyAsync(folder, CREATE);
        return folder;
    }

    @Override
    public void delete(BoxFile file) throws QblStorageException {
        nav.delete(file);
        notifyAsync(file, DELETE);
    }

    @Override
    public void unshare(BoxObject boxObject) throws QblStorageException {
        nav.unshare(boxObject);
        notifyAsync(boxObject, UNSHARE);
    }

    @Override
    public void delete(BoxFolder folder) throws QblStorageException {
        nav.delete(folder);
        cache.remove(folder);
        notifyAsync(folder, DELETE);
    }

    @Override
    public void delete(BoxExternal external) throws QblStorageException {
        nav.delete(external);
    }

    @Override
    public void setAutocommit(boolean autocommit) {
        nav.setAutocommit(autocommit);
    }

    @Override
    public void setAutocommitDelay(long delay) {
        nav.setAutocommitDelay(delay);
    }

    @Override
    public CachedBoxNavigation navigate(String folderName) throws QblStorageException {
        return navigate(getFolder(folderName));
    }

    @Override
    public BoxFolder getFolder(String name) throws QblStorageException {
        return nav.getFolder(name);
    }

    @Override
    public boolean hasFolder(String name) throws QblStorageException {
        return nav.hasFolder(name);
    }

    @Override
    public BoxFile getFile(String name) throws QblStorageException {
        return nav.getFile(name);
    }

    @Override
    public DirectoryMetadata getMetadata() {
        return nav.getMetadata();
    }

    @Override
    public BoxExternalReference createFileMetadata(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
        return nav.createFileMetadata(owner, boxFile);
    }

    @Override
    public void updateFileMetadata(BoxFile boxFile) throws QblStorageException, IOException, InvalidKeyException {
        nav.updateFileMetadata(boxFile);
    }

    @Override
    public BoxExternalReference share(QblECPublicKey owner, BoxFile file, String receiver) throws QblStorageException {
        BoxExternalReference share = nav.share(owner, file, receiver);
        notifyAsync(file, SHARE);
        return share;
    }

    @Override
    public List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException {
        return nav.getSharesOf(object);
    }

    @Override
    public boolean hasVersionChanged(DirectoryMetadata dm) throws QblStorageException {
        return nav.hasVersionChanged(dm);
    }

    public void refresh() throws QblStorageException {
        synchronized (this) {
            synchronized (nav) {
                if (nav.isUnmodified()) {
                    DirectoryMetadata dm = nav.reloadMetadata();
                    if (hasVersionChanged(dm)) {
                        Set<BoxFolder> oldFolders = new HashSet<>(nav.listFolders());
                        Set<BoxFile> oldFiles = new HashSet<>(nav.listFiles());

                        nav.setMetadata(dm);

                        Set<BoxFolder> newFolders = new HashSet<>(nav.listFolders());
                        Set<BoxFile> newFiles = new HashSet<>(nav.listFiles());
                        Set<BoxFile> changedFiles = new HashSet<>();

                        findNewFolders(oldFolders, newFolders);
                        findNewFiles(oldFiles, newFiles, changedFiles);
                        findDeletedFolders(oldFolders, newFolders);
                        findDeletedFiles(oldFiles, newFiles, changedFiles);
                    }
                }
            }
        }

        for (BoxFolder folder : listFolders()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                navigate(folder).refresh();
            } catch (QblStorageException e) {
                logger.error("failed to refresh directory: " + path + "/" + folder.getName() + " " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean hasFile(String name) throws QblStorageException {
        return nav.hasFile(name);
    }

    protected void findDeletedFiles(Set<BoxFile> oldFiles, Set<BoxFile> newFiles, Set<BoxFile> changedFiles) {
        for (BoxFile file : oldFiles) {
            if (changedFiles.contains(file)) {
                continue;
            }
            if (!newFiles.contains(file)) {
                TYPE type = DELETE;
                notify(file, type);
            }
        }
    }

    private void notify(BoxObject file, TYPE type) {
        setChanged();
        Long mtime = file instanceof BoxFile ? ((BoxFile) file).getMtime() : null;
        if (type == DELETE) {
            mtime = System.currentTimeMillis();
        }
        notifyObservers(
                new RemoteChangeEvent(
                        getPath(file),
                        file instanceof BoxFolder,
                        mtime,
                        type,
                        file,
                        this
                )
        );
    }

    protected void findDeletedFolders(Set<BoxFolder> oldFolders, Set<BoxFolder> newFolders) {
        for (BoxFolder folder : oldFolders) {
            if (!newFolders.contains(folder)) {
                notify(folder, DELETE);
            }
        }
    }

    protected void findNewFiles(Set<BoxFile> oldFiles, Set<BoxFile> newFiles, Set<BoxFile> changedFiles) {
        for (BoxFile file : newFiles) {
            if (!oldFiles.contains(file)) {
                TYPE type = CREATE;
                for (BoxFile oldFile : oldFiles) {
                    if (oldFile.getName().equals(file.getName())) {
                        type = UPDATE;
                        changedFiles.add(oldFile);
                        break;
                    }
                }
                notify(file, type);
            }
        }
    }

    protected void findNewFolders(Set<BoxFolder> oldFolders, Set<BoxFolder> newFolders) throws QblStorageException {
        for (BoxFolder folder : newFolders) {
            if (!oldFolders.contains(folder)) {
                notify(folder, CREATE);
                navigate(folder).notifyAllContents();
            }
        }
    }

    public void notifyAllContents() throws QblStorageException {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        // TODO notify async and sync files first (better UX on files)
        for (BoxFolder folder : nav.listFolders()) {
            notify(folder, CREATE);
            navigate(folder).notifyAllContents();
        }
        for (BoxFile file : listFiles()) {
            notify(file, CREATE);
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public Path getPath(BoxObject folder) {
        return BoxFileSystem.get(path).resolve(folder.getName());
    }
}
