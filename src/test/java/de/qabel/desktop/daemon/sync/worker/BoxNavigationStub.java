package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.*;
import de.qabel.box.storage.dto.BoxPath;
import de.qabel.box.storage.dto.DMChangeNotification;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.Observable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.SHARE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UNSHARE;

public class BoxNavigationStub extends Observable implements IndexNavigation {
    public ChangeEvent event;
    public List<BoxFolder> folders = new LinkedList<>();
    public List<BoxFile> files = new LinkedList<>();
    public List<BoxShare> shares = new LinkedList<>();
    public Map<String, BoxNavigationStub> subnavs = new HashMap<>();
    public Subject<DMChangeNotification, DMChangeNotification> subject = PublishSubject.create();
    public Path path;

    public static BoxNavigationStub create() {
        return new BoxNavigationStub(BoxFileSystem.getRoot());
    }

    public BoxNavigationStub(Path path) {
        this.path = path;
    }

    @Override
    public void refresh() throws QblStorageException {
        if (event != null) {
            setChanged();
            notifyObservers(event);
            event = null;
        }
    }

    public void pushNotification(BoxObject object, TYPE type) {
        notifyAsync(object, type);
    }

    private void notifyAsync(BoxObject object, TYPE type) {
        setChanged();
        notifyObservers(new RemoteChangeEvent(
            path.resolve(object.getName()),
            object instanceof BoxFile,
            1000L,
            type,
            object,
            this
        ));
    }

    @Override
    public boolean hasFolder(String name) throws QblStorageException {
        return true;
    }

    @Override
    public BoxNavigationStub navigate(String name) throws QblStorageException {
        if (!subnavs.containsKey(name)) {
            BoxNavigationStub subnav = BoxNavigationStub.create();
            subnav.setDesktopPath(getDesktopPath().resolve(name + "/"));
            subnavs.put(name, subnav);
        }
        return subnavs.get(name);
    }

    public Path getDesktopPath() {
        return path;
    }

    private void setDesktopPath(Path newPath) {
        path = newPath;
    }

    @Override
    public synchronized BoxNavigation navigate(BoxFolder target) throws QblStorageException {
        return navigate(target.getName());
    }

    @Override
    public List<BoxFile> listFiles() throws QblStorageException {
        return files;
    }

    @Override
    public List<BoxFolder> listFolders() throws QblStorageException {
        return folders;
    }

    @Override
    public List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException {
        return shares;
    }

    @Override
    public BoxExternalReference share(QblECPublicKey owner, BoxFile file, String receiver) throws QblStorageException {
        file.setShared(new Share(file.getBlock(), new byte[0]));
        shares.add(new BoxShare(file.getRef(), receiver));
        notifyAsync(file, SHARE);
        return new BoxExternalReference(false, file.getRef(), file.getName(), owner, new byte[0]);
    }

    @Override
    public void unshare(BoxObject boxObject) throws QblStorageException {
        if (!(boxObject instanceof BoxFile)) {
            return;
        }
        if (!((BoxFile) boxObject).isShared()) {
            return;
        }

        BoxFile boxFile = (BoxFile) boxObject;
        shares.stream().sorted()
            .filter(boxShare -> boxFile.getRef().equals(boxShare.getRef()))
            .forEach(shares::remove);
        boxFile.setShared(null);
        notifyAsync(boxObject, UNSHARE);
    }

    @Override
    public BoxFile getFile(String name) throws QblStorageException {
        for (BoxFile file : listFiles()) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        throw new QblStorageNotFound("no file named " + name);
    }

    @NotNull
    @Override
    public DirectoryMetadata reloadMetadata() throws QblStorageException {
        return null;
    }

    @Override
    public void commit() throws QblStorageException {

    }

    @Override
    public void commitIfChanged() throws QblStorageException {

    }

    @NotNull
    @Override
    public BoxFile upload(String s, File file, ProgressListener progressListener) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFile upload(String s, File file) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFile upload(String s, InputStream inputStream, long l, ProgressListener progressListener) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFile upload(String s, InputStream inputStream, long l) throws QblStorageException {
        return null;
    }

    @Override
    public boolean isUnmodified() {
        return false;
    }

    @NotNull
    @Override
    public BoxFile overwrite(String s, File file, ProgressListener progressListener) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFile overwrite(String s, File file) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public InputStream download(BoxFile boxFile, ProgressListener progressListener) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public InputStream download(String s) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public InputStream download(BoxFile boxFile) throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public FileMetadata getFileMetadata(BoxFile boxFile) throws IOException, InvalidKeyException, QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public FileMetadata getMetadataFile(Share share) throws IOException, InvalidKeyException, QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFolder createFolder(String s) throws QblStorageException {
        return null;
    }

    @Override
    public void delete(BoxFile boxFile) throws QblStorageException {

    }

    @Override
    public void delete(BoxFolder boxFolder) throws QblStorageException {

    }

    @Override
    public void delete(BoxExternal boxExternal) throws QblStorageException {

    }

    @Override
    public void setAutocommit(boolean b) {

    }

    @Override
    public void setAutocommitDelay(long l) {

    }

    @NotNull
    @Override
    public DirectoryMetadata getMetadata() {
        return null;
    }

    @Override
    public void setMetadata(DirectoryMetadata directoryMetadata) {

    }

    @NotNull
    @Override
    public BoxExternalReference getExternalReference(QblECPublicKey qblECPublicKey, BoxFile boxFile) {
        return null;
    }

    @Override
    public boolean hasVersionChanged(DirectoryMetadata directoryMetadata) throws QblStorageException {
        return false;
    }

    @Override
    public void visit(Function2<? super AbstractNavigation, ? super BoxObject, Unit> function2) {

    }

    @NotNull
    @Override
    public BoxPath.FolderLike getPath() {
        return null;
    }

    @NotNull
    @Override
    public rx.Observable<DMChangeNotification> getChanges() {
        return subject;
    }

    @NotNull
    @Override
    public BoxNavigation navigate(BoxExternal boxExternal) {
        return null;
    }

    @NotNull
    @Override
    public List<BoxExternal> listExternals() throws QblStorageException {
        return null;
    }

    @NotNull
    @Override
    public BoxFolder getFolder(String s) throws QblStorageException {
        return null;
    }

    @Override
    public boolean hasFile(String s) throws QblStorageException {
        return false;
    }

    @Override
    public void refresh(boolean b) throws QblStorageException {

    }

    @NotNull
    @Override
    public List<BoxShare> listShares() throws QblStorageException {
        return null;
    }

    @Override
    public void insertShare(BoxShare boxShare) throws QblStorageException {

    }

    @Override
    public void deleteShare(BoxShare boxShare) throws QblStorageException {

    }
}
