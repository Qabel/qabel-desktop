package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.*;
import de.qabel.box.storage.command.DirectoryMetadataChange;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FolderTreeItemTest extends AbstractControllerTest {
    private FakeBoxNavigation navigation;
    private FolderTreeItem item;

    @Test(timeout = 1000)
    public void adjustsNameProperty() {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        StringProperty nameProperty = item.getNameProperty();

        assertEquals("name", nameProperty.get());

        ((FakeBoxNavigation) item.getNavigation()).loading = true;
        item.getChildren();

        assertEquals("name (loading)", nameProperty.get());

        ((FakeBoxNavigation) item.getNavigation()).loading = false;
        load();

        waitUntil(() -> nameProperty.get().equals("name"));
        assertEquals("name", nameProperty.get());
    }

    @Test(timeout = 1000)
    public void isLeafWithoutFiles() {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        load();
        waitUntil(() -> item.isLeaf());
    }

    @Test(timeout = 1000)
    public void synchronouslyReturnsEmptyList() {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        navigation.folders.add(new BoxFolder("ref2", "name2", new byte[0]));
        navigation.loading = true;

        assertEquals(0, item.getChildren().size());
        navigation.loading = false;
    }

    @Test(timeout = 10000)
    public void loadsChildrenAsynchonously() throws InterruptedException {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        navigation.folders.add(createSomeFolder());

        ObservableList<TreeItem<BoxObject>> children = load();

        waitUntil(() -> children.size() == 1);
        assertFalse(item.isLeaf());
    }

    private BoxFolder createSomeFolder() {
        return new BoxFolder("ref", "name", new byte[0]);
    }

    private ObservableList<TreeItem<BoxObject>> load() {
        ObservableList<TreeItem<BoxObject>> children = item.getChildren();
        ((FakeBoxNavigation) item.getNavigation()).loading = false;
        while (item.isLoading())
            Thread.yield();
        return children;
    }

    @Test(timeout = 1000)
    public void loadsFiles() throws Exception {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        navigation.files.add(createSomeFile());

        ObservableList<TreeItem<BoxObject>> children = load();
        waitUntil(() -> children.size() == 1);
    }

    @Test
    public void updatesOnNavChange() throws Exception {
        navigation = new FakeBoxNavigation();
        item = new FolderTreeItem(createSomeFolder(), navigation);
        item.setExpanded(true);
        List children = load();
        assertEquals(0, children.size());

        navigation.files.add(createSomeFile());
        navigation.setChanged();
        navigation.notifyObservers(new RemoteChangeEvent(
            Paths.get("/name2"),
            false,
            navigation.files.get(0).getMtime(),
            ChangeEvent.TYPE.CREATE,
            navigation.files.get(0),
            navigation
        ));
        waitUntil(() -> children.size() == 1);
    }

    private BoxFile createSomeFile() {
        return new BoxFile("prefix", "ref2", "name2", 0L, 0L, new byte[0], null, null);
    }

    private class FakeBoxNavigation extends Observable implements BoxNavigation {
        public boolean loading;

        public List<BoxFile> files = new LinkedList<>();
        public List<BoxFolder> folders = new LinkedList<>();

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

        @Override
        public BoxNavigation navigate(BoxFolder target) throws QblStorageException {
            return null;
        }

        @Override
        public BoxNavigation navigate(BoxExternal target) {
            return null;
        }

        @Override
        public List<BoxFile> listFiles() throws QblStorageException {
            while (loading)
                Thread.yield();
            return files;
        }

        @Override
        public List<BoxFolder> listFolders() throws QblStorageException {
            while (loading)
                Thread.yield();
            return folders;
        }

        @Override
        public List<BoxExternal> listExternals() throws QblStorageException {
            return null;
        }

        @Override
        public BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException {
            return null;
        }

        @Override
        public boolean isUnmodified() {
            return false;
        }

        @Override
        public BoxFile upload(String name, File file) throws QblStorageException {
            return null;
        }

        @Override
        public BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException {
            return null;
        }

        @Override
        public BoxFile overwrite(String name, File file) throws QblStorageException {
            return null;
        }

        @Override
        public InputStream download(BoxFile file, ProgressListener listener) throws QblStorageException {
            return null;
        }

        @Override
        public InputStream download(BoxFile file) throws QblStorageException {
            return null;
        }

        @Override
        public FileMetadata getFileMetadata(BoxFile boxFile) throws IOException, InvalidKeyException, QblStorageException {
            return null;
        }

        @Override
        public BoxFolder createFolder(String name) throws QblStorageException {
            return null;
        }

        @Override
        public void delete(BoxFile file) throws QblStorageException {

        }

        @Override
        public void unshare(BoxObject boxObject) throws QblStorageException {

        }

        @Override
        public void delete(BoxFolder folder) throws QblStorageException {

        }

        @Override
        public void delete(BoxExternal external) throws QblStorageException {

        }

        @Override
        public void setAutocommit(boolean autocommit) {

        }

        @Override
        public void setAutocommitDelay(long delay) {

        }

        @Override
        public BoxNavigation navigate(String folderName) throws QblStorageException {
            return null;
        }

        @Override
        public BoxFolder getFolder(String name) throws QblStorageException {
            return null;
        }

        @Override
        public boolean hasFolder(String name) throws QblStorageException {
            return false;
        }

        @Override
        public BoxFile getFile(String name) throws QblStorageException {
            return null;
        }

        @Override
        public JdbcDirectoryMetadata getMetadata() {
            return null;
        }

        @Override
        public BoxExternalReference createFileMetadata(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
            return null;
        }

        @Override
        public void updateFileMetadata(BoxFile boxFile) throws QblStorageException, IOException, InvalidKeyException {

        }

        @Override
        public BoxExternalReference share(QblECPublicKey owner, BoxFile file, String receiver) throws QblStorageException {
            return null;
        }

        @Override
        public List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException {
            return null;
        }

        public boolean hasVersionChanged(JdbcDirectoryMetadata dm) throws QblStorageException {
            return false;
        }

        @Override
        public boolean hasFile(String name) throws QblStorageException {
            return false;
        }

        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }

        @Override
        public void notifyObservers() {
            super.notifyObservers();
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

        @NotNull
        @Override
        public InputStream download(String s) throws QblStorageException {
            return null;
        }

        @Override
        public void setMetadata(DirectoryMetadata directoryMetadata) {

        }

        @Override
        public boolean hasVersionChanged(DirectoryMetadata directoryMetadata) throws QblStorageException {
            return false;
        }

        @Override
        public void refresh() throws QblStorageException {

        }

        @Override
        public void refresh(boolean b) throws QblStorageException {

        }

        @NotNull
        @Override
        public rx.Observable<DirectoryMetadataChange<Object>> getChanges() {
            return null;
        }

        @NotNull
        @Override
        public FileMetadata getMetadataFile(Share share) throws IOException, InvalidKeyException, QblStorageException {
            return null;
        }
    }
}
