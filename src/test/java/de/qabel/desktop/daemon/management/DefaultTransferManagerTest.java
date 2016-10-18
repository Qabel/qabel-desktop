package de.qabel.desktop.daemon.management;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxNavigation;
import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.factory.LocalBoxVolumeFactory;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DefaultTransferManagerTest extends AbstractSyncTest {
    public static final long NEWER = 10000L;
    public static final long OLDER = -10000L;
    private BoxVolume volume;
    private UploadStub upload;
    private DefaultTransferManager manager;
    private DownloadStub download;
    private BoxPath syncRoot = BoxFileSystem.getRoot().resolve("syncRoot");

    private Path tmpPath(String dir) {
        return Paths.get(tmpDir.toString(), dir);
    }

    private BoxNavigation nav() throws QblStorageException {
        return volume.navigate();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            Account account = new Account("a", "b", "c");
            Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).build();
            volume = new LocalBoxVolumeFactory(tmpDir.toFile(), "abc", "prefix").getVolume(account, identity);
            volume.createIndex("??");

            upload = new UploadStub();
            upload.volume = volume;
            upload.stagingDelay = 10L;

            download = new DownloadStub();
            download.volume = volume;

            manager = new DefaultTransferManager();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Override
    @After
    public void tearDown() throws InterruptedException {
        super.tearDown();
    }

    @Test
    public void queuesUploads() {
        Upload upload = new DummyUpload();
        manager.addUpload(upload);
        assertEquals(1, manager.getTransactions().size());
        assertSame(upload, manager.getTransactions().get(0));
    }

    @Test
    public void createsRootDirectory() throws QblStorageException {
        upload.source = tmpPath("/syncRoot");
        upload.destination = syncRoot;
        upload.source.toFile().mkdirs();
        upload.isDir = true;

        manager.upload(upload);

        List<BoxFolder> folders = nav().listFolders();
        assertEquals(1, folders.size());
        assertEquals("syncRoot", folders.get(0).getName());
    }

    @Test
    public void closesUpload() throws Exception {
        upload.source = tmpPath("/syncRoot");
        upload.destination = syncRoot;
        upload.source.toFile().mkdir();
        upload.isDir = true;

        manager.upload(upload);

        assertEquals(FINISHED, upload.state);
        assertTrue("upload was not closed", upload.closed);
    }

    @Test
    public void createsSubDirectories() throws Exception {
        nav().createFolder("syncRoot");
        upload.source = tmpPath("/syncRoot/subdir");
        upload.source.toFile().mkdirs();
        upload.destination = syncRoot.resolve("targetSubdir");

        upload.isDir = true;
        manager.upload(upload);

        List<BoxFolder> folders = nav().navigate("syncRoot").listFolders();
        assertEquals(1, folders.size());
        assertEquals("targetSubdir", folders.get(0).getName());
    }

    @Test
    public void uploadsFiles() throws Exception {
        nav().createFolder("syncRoot");
        upload.source = tmpPath("file");
        upload.destination = syncRoot.resolve("targetFile");
        File sourceFile = upload.source.toFile();
        write("testcontent", upload.source);
        upload.isDir = false;

        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        List<BoxFile> files = syncRoot.listFiles();
        assertEquals(1, files.size());
        BoxFile boxFile = files.get(0);
        assertEquals("targetFile", boxFile.getName());
        assertEquals("testcontent", IOUtils.toString(syncRoot.download(boxFile)));

        assertTrue(upload.getSize() > sourceFile.length());
        long encryptedSize = upload.getSize();
        assertEquals(encryptedSize, upload.getTransferred());
    }

    @Test
    public void deletesDeletedFolders() throws Exception {
        nav().createFolder("syncRoot");
        UploadStub setupUpload = new UploadStub();
        setupUpload.volume = volume;
        setupUpload.source = tmpPath("syncRoot/folder");
        setupUpload.source.toFile().mkdirs();
        setupUpload.destination = syncRoot.resolve("folder");
        manager.upload(setupUpload);

        upload.source = tmpPath("syncRoot/folder");
        upload.destination = syncRoot.resolve("folder");
        upload.type = DELETE;
        upload.isDir = false;   // may not be detectable if folder is already gone

        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        assertEquals(0, syncRoot.listFolders().size());
    }

    @Test
    public void deletesDeletedFile() throws Exception {
        nav().createFolder("syncRoot");
        UploadStub fileUpload = new UploadStub();
        fileUpload.volume = volume;
        fileUpload.source = tmpPath("file");
        write("wayne", fileUpload.source);
        fileUpload.destination = syncRoot.resolve("targetFile");
        manager.upload(fileUpload);

        upload.source = tmpPath("file");
        upload.destination = syncRoot.resolve("targetFile");
        upload.type = DELETE;
        upload.mtime = System.currentTimeMillis() + NEWER;
        upload.isDir = false;
        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        List<BoxFile> files = syncRoot.listFiles();
        assertEquals(0, files.size());
    }

    @Test
    public void updatesFiles() throws Exception {
        nav().createFolder("syncRoot");
        upload.isDir = false;
        upload.source = tmpPath("file");
        upload.destination = syncRoot.resolve("targetFile");
        write("testcontent", upload.source);
        upload.mtime = modifyMtime(upload.source, OLDER);
        manager.upload(upload);

        upload.type = UPDATE;
        write("content2", upload.source);
        upload.mtime = modifyMtime(upload.source, NEWER);
        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        List<BoxFile> files = syncRoot.listFiles();
        assertEquals(1, files.size());
        BoxFile boxFile = files.get(0);
        assertEquals("targetFile", boxFile.getName());
        assertEquals("content2", IOUtils.toString(syncRoot.download(boxFile)));
    }

    @Test
    public void handlesFalseCreatesLikeUpdates() throws Exception {
        nav().createFolder("syncRoot");
        upload.source = tmpPath("file");
        write("testcontent", upload.source);
        upload.destination = syncRoot.resolve("targetFile");
        upload.mtime = modifyMtime(upload.source, NEWER);
        upload.isDir = false;
        upload.type = CREATE;
        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        List<BoxFile> files = syncRoot.listFiles();
        assertEquals(1, files.size());
        BoxFile boxFile = files.get(0);
        assertEquals("targetFile", boxFile.getName());
        assertEquals("testcontent", IOUtils.toString(syncRoot.download(boxFile)));
    }

    @Test
    public void handlesFalseUpdatesLikeCreates() throws Exception {
        nav().createFolder("syncRoot");
        upload.type = UPDATE;
        upload.source = tmpPath("file");
        upload.destination = syncRoot.resolve("targetFile");
        File sourceFile = upload.source.toFile();
        sourceFile.createNewFile();
        write("testcontent", upload.source);
        upload.isDir = false;

        manager.upload(upload);

        BoxNavigation syncRoot = nav().navigate("syncRoot");
        List<BoxFile> files = syncRoot.listFiles();
        assertEquals(1, files.size());
        BoxFile boxFile = files.get(0);
        assertEquals("targetFile", boxFile.getName());
        assertEquals("testcontent", IOUtils.toString(syncRoot.download(boxFile)));
    }

    private void write(String content, Path file) throws IOException {
        Files.write(file, content.getBytes());
    }

    @Test
    public void downloadsFolders() throws Exception {
        nav().createFolder("syncRoot");
        download.source = syncRoot;
        download.destination = tmpPath("syncLocal");
        download.type = CREATE;
        download.isDir = true;

        manager.download(download);

        assertTrue(Files.isDirectory(download.destination));
    }

    @Test
    public void closesDownload() throws Exception {
        nav().createFolder("syncRoot");
        download.source = syncRoot;
        download.destination = tmpPath("syncLocal");
        download.type = CREATE;
        download.isDir = true;

        manager.download(download);

        assertEquals(FINISHED, download.state);
        assertTrue("download not closed", download.closed);
    }

    @Test
    public void downloadsFiles() throws Exception {
        Path downloadPath = tmpPath("testfile");
        write("testcontent", downloadPath);
        File file = downloadPath.toFile();
        lastUpload = nav().upload("testfile", file);
        download.isDir = true;
        file.delete();

        setDownload(downloadPath, 0L, CREATE, false);
        manager.download(download);

        assertTrue(Files.exists(downloadPath));
        assertEquals("testcontent", new String(Files.readAllBytes(downloadPath)));
    }

    private BoxFile lastUpload;

    protected Path uploadFile(String content, String filename) throws IOException, QblStorageException {
        Path downloadPath = tmpPath(filename);
        write(content, downloadPath);
        File file = downloadPath.toFile();
        lastUpload = nav().upload(filename, file);
        return downloadPath;
    }

    @Test
    public void updatesLocalFiles() throws Exception {
        Path downloadPath = uploadFile("testcontent", "testfile");
        write("something else", downloadPath);
        modifyMtime(downloadPath, -10000L);

        setDownload(downloadPath, 0L, UPDATE, false);
        manager.download(download);

        assertEquals("testcontent", new String(Files.readAllBytes(downloadPath)));
    }

    protected long modifyMtime(Path downloadPath, long diff) throws IOException {
        long newMtime = Files.getLastModifiedTime(downloadPath).toMillis() + diff;
        Files.setLastModifiedTime(downloadPath, FileTime.fromMillis(newMtime));
        return newMtime;
    }

    @Test
    public void doesntDownloadOlderFiles() throws Exception {
        Path downloadPath = uploadFile("testcontent", "testfile");
        write("newcontent", downloadPath);
        modifyMtime(downloadPath, NEWER);

        setDownload(downloadPath, 0L, UPDATE, false);
        manager.download(download);

        assertEquals(SKIPPED, download.getState());
        assertEquals("newcontent", new String(Files.readAllBytes(downloadPath)));
    }

    @Test
    public void doesntDownloadOnOutdatedDownload() throws Exception {
        Path downloadPath = uploadFile("testcontent", "testfile");
        write("not downloaded", downloadPath);
        modifyMtime(downloadPath, OLDER);

        setDownload(downloadPath, OLDER / 2, UPDATE, false);
        manager.download(download);

        assertEquals(SKIPPED, download.getState());
        assertEquals("not downloaded", new String(Files.readAllBytes(downloadPath)));
    }

    @Test
    public void deletesFilesLocally() throws Exception {
        Path downloadPath = tmpPath("testfile");
        write("content", downloadPath);

        setDownload(downloadPath, NEWER, DELETE, false);
        manager.download(download);

        assertFalse("local file was not deleted", Files.exists(downloadPath));
    }

    private void setDownload(Path destination, long mtimeDiff, Transaction.TYPE type, boolean isDir) throws IOException {
        download.source = BoxFileSystem.getRoot().resolve("testfile");
        download.destination = destination;
        download.mtime = lastUpload != null ? lastUpload.getMtime() + mtimeDiff : Files.getLastModifiedTime(destination).toMillis() + mtimeDiff;
        download.type = type;
        download.isDir = isDir;
    }

    @Test
    public void doesntUploadOlderFiles() throws Exception {
        uploadFile("content", "testfile");

        Path source = tmpPath("testfile");
        write("newercontent", source);
        long mtime = modifyMtime(source, OLDER);
        upload.source = source;
        upload.destination = BoxFileSystem.getRoot().resolve("testfile");
        upload.mtime = mtime;
        upload.type = UPDATE;
        upload.isDir = false;
        manager.upload(upload);

        assertEquals(SKIPPED, upload.getState());
        assertRemoteExists("content", "testfile");
    }

    @Test
    public void doesntDeleteNewerRemote() throws Exception {
        uploadFile("content", "testfile");

        upload.source = tmpPath("wayne");
        upload.destination = BoxFileSystem.getRoot().resolve("testfile");
        upload.mtime = lastUpload.getMtime() + OLDER;
        upload.type = DELETE;
        upload.isDir = false;
        manager.upload(upload);

        assertEquals(SKIPPED, upload.getState());
        assertRemoteExists("content", "testfile");
    }

    @Test
    public void skipsTempFilesWithStagingArea() throws Exception {
        upload.stagingDelay = 500L;

        File file = createValidUpload();
        manager.addUpload(upload);
        managerNext();

        waitUntil(() -> upload.getState() == WAITING);
        file.delete();
        upload.valid = false;

        waitUntil(() -> upload.getState() == SKIPPED);
    }

    private File createValidUpload() throws IOException {
        Path path = tmpPath("file");
        File file = path.toFile();
        file.createNewFile();
        upload.source = path;
        upload.destination = BoxFileSystem.getRoot().resolve("testfile");
        upload.mtime = Files.getLastModifiedTime(path).toMillis();
        upload.type = CREATE;
        upload.transactionAge = 0L;
        upload.isDir = false;
        return file;
    }

    @Test(timeout = 1000L)
    public void skipsCancelledDownloads() throws Exception {
        createValidUpload();
        upload.toState(FAILED);
        manager.addUpload(upload);
        manager.next();
        assertThat(manager.getTransactions(), is(empty()));
        assertThat(upload.state, is(FAILED));
    }

    protected void managerNext() {
        new Thread(() -> {
            try {
                manager.next();
            } catch (InterruptedException e) {
                AbstractControllerTest.createLogger().warn("interrupted while processing transfers", e);
            }
        }).start();
    }

    @Test
    public void downloadsAreNotStaged() throws Exception {
        download.stagingDelay = 2000L;

        download.source = BoxFileSystem.getRoot().resolve("wayne");
        download.destination = tmpPath("wayne");
        download.mtime = 1000L;
        download.type = CREATE;
        download.isDir = false;
        manager.addDownload(download);
        managerNext();

        waitUntil(() -> download.getState() == FAILED, () -> download.getState().toString());
    }

    @Test
    public void updatesDownloadSize() throws Exception {
        Path file = tmpPath("file");
        Files.write(file, "1234567890".getBytes());
        volume.navigate().upload("file", file.toFile());
        download.source = BoxFileSystem.getRoot().resolve("file");
        download.destination = tmpPath("wayne");
        download.mtime = 10L;
        download.type = CREATE;
        download.isDir = false;
        manager.addDownload(download);
        managerNext();

        waitUntil(
                () -> download.hasSize() && download.getSize() == 10,
                () -> download.hasSize() ? download.getSize() + " != 10" : "no size"
        );
    }

    @Test
    public void updatesDownloadProgress() throws Exception {
        Path file = tmpPath("file");
        Files.write(file, "1234567890".getBytes());
        BoxFile remote = volume.navigate().upload("file", file.toFile());
        download.source = BoxFileSystem.getRoot().resolve("file");
        download.destination = tmpPath("wayne");
        download.mtime = remote.getMtime();
        download.type = CREATE;
        download.isDir = false;
        manager.addDownload(download);
        managerNext();

        waitUntil(() -> download.hasSize());
        waitUntil(() -> download.getTransferred() == download.getSize(), () -> {
            return download.getTransferred() + " != " + download.getSize();
        });
    }

    private void assertRemoteExists(String content, String testfile) throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        BoxFile file = nav.getFile(testfile);
        assertEquals(content, IOUtils.toString(nav.download(file)));
    }
}
