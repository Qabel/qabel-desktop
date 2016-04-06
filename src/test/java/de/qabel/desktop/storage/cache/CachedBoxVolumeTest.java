package de.qabel.desktop.storage.cache;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

public class CachedBoxVolumeTest extends BoxVolumeTest {
    private Path tempFolder;
    private List<ChangeEvent> updates = new LinkedList<>();
    private CachedBoxNavigation nav;
    private LocalReadBackend readBackend;

    @Override
    protected StorageReadBackend getReadBackend() {
        return readBackend;
    }

    @Override
    protected void setUpVolume() throws IOException {
        AbstractNavigation.DEFAULT_AUTOCOMMIT_DELAY = 0;
        tempFolder = Files.createTempDirectory("");

        readBackend = new LocalReadBackend(tempFolder);
        volume = new CachedBoxVolume(readBackend,
                new LocalWriteBackend(tempFolder),
                keyPair, deviceID, volumeTmpDir, "");
        volume2 = new CachedBoxVolume(new LocalReadBackend(tempFolder),
                new LocalWriteBackend(tempFolder),
                keyPair, deviceID2, volumeTmpDir, "");
    }

    @Override
    protected void cleanVolume() throws IOException {
        FileUtils.deleteDirectory(tempFolder.toFile());
    }

    @Test
    public void providesCachedNavigations() throws Exception {
        assertTrue(volume.navigate() instanceof PathNavigation);
        assertSame(volume.navigate(), volume.navigate());
    }

    @Test
    public void navigationReturnsCachedNavigationsItself() throws Exception {
        CachedBoxNavigation nav = (CachedBoxNavigation) volume.navigate();
        BoxFolder subfolder = nav.createFolder("subfolder");
        BoxNavigation subnav = nav.navigate(subfolder);
        subnav.createFolder("marker");

        assertTrue(subnav instanceof PathNavigation);
        assertTrue(nav.navigate("subfolder") instanceof PathNavigation);
        assertEquals("marker", subnav.listFolders().get(0).getName());
        assertSame(subnav, nav.navigate("subfolder"));
    }

    @Test
    public void cachesFolderList() throws Exception {
        BoxNavigation nav = volume.navigate();
        nav.createFolder("test");
        nav.listFolders();

        BoxNavigation nav2 = volume2.navigate();
        nav2.delete(nav2.getFolder("test"));

        assertEquals(1, nav.listFolders().size());
    }

    @Test
    public void refreshesFolderListOnCreate() throws Exception {
        BoxNavigation nav = volume.navigate();
        nav.listFolders();

        nav.createFolder("folder");
        assertEquals(1, nav.listFolders().size());
    }

    @Test
    public void isRefreshable() throws Exception {
        CachedBoxNavigation nav = (CachedBoxNavigation) volume.navigate();
        BoxNavigation nav2 = volume2.navigate();
        nav2.createFolder("test");

        nav.refresh();
        assertEquals(1, nav.listFolders().size());
    }

    @Test
    public void refreshesRecursive() throws Exception {
        CachedBoxNavigation nav = (CachedBoxNavigation) volume.navigate();
        nav.createFolder("folder");
        CachedBoxNavigation subnav = nav.navigate("folder");

        BoxNavigation nav2 = volume2.navigate();
        nav2.navigate("folder").createFolder("subfolder");

        nav.refresh();
        assertEquals(1, subnav.listFolders().size());
    }

    @Test
    public void notifiesOnNewDirectory() throws Exception {
        observe();

        BoxNavigation nav2 = volume2.navigate();
        nav2.createFolder("folder");
        nav.refresh();

        assertFalse(updates.isEmpty());
        ChangeEvent event = updates.get(0);
        assertTrue(event.isCreate());
        assertEquals("/folder", event.getPath().toString());
    }

    @Test
    public void notifiesOnContentOfNewDirectory() throws Exception {
        observe();

        BoxNavigation nav2 = volume2.navigate();
        BoxFolder folder = nav2.createFolder("folder");
        BoxNavigation foldernav2 = nav2.navigate(folder);
        BoxFolder subfolder = foldernav2.createFolder("subfolder");
        File file = createTmpFile();
        foldernav2.upload("testfile", file);
        foldernav2.navigate(subfolder).upload("testfile", file);

        nav.refresh();

        Set<String> foundPaths = new HashSet<>();
        updates.forEach(changeEvent -> foundPaths.add(changeEvent.getPath().toString()));
        assertThat(foundPaths, hasItem("/folder"));
        assertThat(foundPaths, hasItem("/folder/subfolder"));
        assertThat(foundPaths, hasItem("/folder/testfile"));
        assertThat(foundPaths, hasItem("/folder/subfolder/testfile"));
    }

    protected void observe() throws QblStorageException {
        updates.clear();
        nav = (CachedBoxNavigation) volume.navigate();
        nav.addObserver((o, arg) -> updates.add((ChangeEvent) arg));
    }

    @Test
    public void notifiesOnNewFile() throws Exception {
        observe();
        File file = createTmpFile();
        volume2.navigate().upload("testfile", file);
        nav.refresh();

        assertFalse(updates.isEmpty());
        ChangeEvent event = updates.get(0);
        assertTrue(event.isCreate());
        assertEquals("/testfile", event.getPath().toString());
    }

    public File createTmpFile() throws IOException {
        File file = Paths.get(tempFolder.toAbsolutePath().toString(), "tmpfile").toFile();
        file.createNewFile();
        file.deleteOnExit();
        return file;
    }

    @Test
    public void notifiesOnShare() throws Exception {
        File file = createTmpFile();
        BoxFile boxFile = volume2.navigate().upload("testfile", file);
        ((CachedBoxVolume)volume).navigate().refresh();

        observe();
        nav.share(new QblECKeyPair().getPub(), boxFile, "receiver");

        ChangeEvent event = waitForEvent();
        assertTrue("expected SHARE but got " + event.getType(), event.isShare());
        assertEquals("/testfile", event.getPath().toString());
    }

    private ChangeEvent waitForEvent() {
        waitUntil(() -> !updates.isEmpty());
        return updates.get(0);
    }

    @Test
    public void notifiesOnUnshare() throws Exception {
        File file = createTmpFile();
        BoxFile boxFile = volume2.navigate().upload("testfile", file);
        volume2.navigate().share(new QblECKeyPair().getPub(), boxFile, "receiver");
        ((CachedBoxVolume)volume).navigate().refresh();
        observe();

        nav.unshare(nav.getFile("testfile"));

        ChangeEvent event = waitForEvent();
        assertTrue("expected UNSHARE but got " + event.getType(), event.isUnshare());
        assertEquals("/testfile", event.getPath().toString());
    }

    @Test
    public void notifiesOnChangedFile() throws Exception {
        observe();
        Path tmpfile = Paths.get(tempFolder.toAbsolutePath().toString(), "tmpfile");
        Files.write(tmpfile, "content1".getBytes());
        File file = tmpfile.toFile();
        file.deleteOnExit();
        nav.upload("testfile", file);
        clear(1);

        Files.write(tmpfile, "content2".getBytes());
        volume2.navigate().overwrite("testfile", file);
        nav.refresh();

        assertEquals("no notification", 1, updates.size());
        ChangeEvent event = updates.get(0);
        assertTrue("wrong event type " + event.getType(), event.isUpdate());
        assertEquals("/testfile", event.getPath().toString());
    }

    @Test
    public void notifiesOnDeleteFolder() throws Exception {
        observe();
        nav.createFolder("testfolder");
        clear(1);

        BoxNavigation nav2 = volume2.navigate();
        nav2.delete(nav2.getFolder("testfolder"));
        nav.refresh();

        waitUntil(() -> updates.size() == 1, 10000L, () -> "no notification");
        ChangeEvent event = updates.get(0);
        assertTrue("wrong event type " + event.getType(), event.isDelete());
        assertEquals("/testfolder", event.getPath().toString());
    }

    @Test
    public void notifiesOnDeleteFile() throws Exception {
        observe();
        File file = createTmpFile();
        BoxFile boxFile = nav.upload("testfile", file);
        clear(1);

        volume2.navigate().delete(boxFile);
        nav.refresh();

        waitUntil(() -> updates.size() == 1, () -> "no notification");
        ChangeEvent event = updates.get(0);
        assertTrue("wrong event type " + event.getType(), event.isDelete());
        assertEquals("/testfile", event.getPath().toString());
    }

    protected void clear(int events) {
        waitUntil(() -> updates.size() == events);
        updates.clear();
    }

    @Test
    public void notifiesOnSubnavigationChanges() throws Exception {
        observe();
        nav.navigate(nav.createFolder("folder"));
        clear(1);

        volume2.navigate().navigate("folder").createFolder("subchange");
        nav.refresh();

        assertEquals("no notification", 1, updates.size());
        ChangeEvent event = updates.get(0);
        assertEquals("/folder/subchange", event.getPath().toString());
    }
}
