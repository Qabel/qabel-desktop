package de.qabel.desktop.daemon.management;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.config.factory.LocalBoxVolumeFactory;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxVolume;
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
import java.util.concurrent.TimeUnit;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.*;
import static org.junit.Assert.*;

public class DefaultLoadManagerTest extends AbstractSyncTest {
	public static final long NEWER = 10000L;
	public static final long OLDER = -10000L;
	private BoxVolume volume;
	private UploadStub upload;
	private DefaultLoadManager manager;
	private DownloadStub download;

	private Path tmpPath(String dir) {
		return Paths.get(tmpDir.toString(), dir);
	}

	private BoxNavigation nav() throws QblStorageException {
		return volume.navigate();
	}

	@Before
	public void setUp() {
		super.setUp();
		try {
			Account account = new Account("a", "b", "c");
			Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).build();
			volume = new LocalBoxVolumeFactory(tmpDir, "abc").getVolume(account, identity);
			volume.createIndex("??");

			upload = new UploadStub();
			upload.volume = volume;

			download = new DownloadStub();
			download.volume = volume;

			manager = new DefaultLoadManager();
			manager.setStagingDelay(10L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

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
		upload.destination = Paths.get("syncRoot");
		upload.source.toFile().mkdirs();
		upload.isDir = true;

		manager.upload(upload);

		List<BoxFolder> folders = nav().listFolders();
		assertEquals(1, folders.size());
		assertEquals("syncRoot", folders.get(0).name);
	}

	@Test
	public void closesUpload() throws Exception {
		upload.source = tmpPath("/syncRoot");
		upload.destination = Paths.get("syncRoot");
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
		upload.destination = Paths.get("/syncRoot/targetSubdir");

		upload.isDir = true;
		manager.upload(upload);

		List<BoxFolder> folders = nav().navigate("syncRoot").listFolders();
		assertEquals(1, folders.size());
		assertEquals("targetSubdir", folders.get(0).name);
	}

	@Test
	public void uploadsFiles() throws Exception {
		nav().createFolder("syncRoot");
		upload.source = tmpPath("file");
		upload.destination = Paths.get("/syncRoot", "targetFile");
		File sourceFile = upload.source.toFile();
		sourceFile.createNewFile();
		write("testcontent", upload.source);
		upload.isDir = false;

		manager.upload(upload);

		BoxNavigation syncRoot = nav().navigate("syncRoot");
		List<BoxFile> files = syncRoot.listFiles();
		assertEquals(1, files.size());
		BoxFile boxFile = files.get(0);
		assertEquals("targetFile", boxFile.name);
		assertEquals("testcontent", IOUtils.toString(syncRoot.download(boxFile)));
	}

	@Test
	public void deletesDeletedFolders() throws Exception {
		nav().createFolder("syncRoot");
		UploadStub setupUpload = new UploadStub();
		setupUpload.volume = volume;
		setupUpload.source = tmpPath("syncRoot/folder");
		setupUpload.source.toFile().mkdirs();
		setupUpload.destination = Paths.get("/syncRoot", "folder");
		manager.upload(setupUpload);

		upload.source = tmpPath("syncRoot/folder");
		upload.destination = Paths.get("/syncRoot/folder");
		upload.type = DELETE;
		upload.isDir = false;	// may not be detectable if folder is already gone

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
		fileUpload.destination = Paths.get("/syncRoot", "targetFile");
		manager.upload(fileUpload);

		upload.source = tmpPath("file");
		upload.destination = Paths.get("/syncRoot", "targetFile");
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
		upload.source = tmpPath("file");
		upload.destination = Paths.get("/syncRoot", "targetFile");
		write("testcontent", upload.source);
		upload.mtime = modifyMtime(upload.source, NEWER);
		manager.upload(upload);
		upload.isDir = false;

		upload.type = Transaction.TYPE.UPDATE;
		write("content2", upload.source);
		manager.upload(upload);

		BoxNavigation syncRoot = nav().navigate("syncRoot");
		List<BoxFile> files = syncRoot.listFiles();
		assertEquals(1, files.size());
		BoxFile boxFile = files.get(0);
		assertEquals("targetFile", boxFile.name);
		assertEquals("content2", IOUtils.toString(syncRoot.download(boxFile)));
	}

	@Test
	public void handlesFalseCreatesLikeUpdates() throws Exception {
		nav().createFolder("syncRoot");
		upload.source = tmpPath("file");
		upload.destination = Paths.get("/syncRoot", "targetFile");
		write("testcontent", upload.source);
		upload.mtime = modifyMtime(upload.source, NEWER);
		manager.upload(upload);
		upload.isDir = false;

		upload.type = Transaction.TYPE.CREATE;
		write("content2", upload.source);
		manager.upload(upload);

		BoxNavigation syncRoot = nav().navigate("syncRoot");
		List<BoxFile> files = syncRoot.listFiles();
		assertEquals(1, files.size());
		BoxFile boxFile = files.get(0);
		assertEquals("targetFile", boxFile.name);
		assertEquals("content2", IOUtils.toString(syncRoot.download(boxFile)));
	}

	@Test
	public void handlesFalseUpdatesLikeCreates() throws Exception {
		// todo
	}

	private void write(String content, Path file) throws IOException {
		Files.write(file, content.getBytes());
	}

	@Test
	public void downloadsFolders() throws Exception {
		nav().createFolder("syncRoot");
		download.source = Paths.get("/syncRoot");
		download.destination = tmpPath("syncLocal");
		download.type = Transaction.TYPE.CREATE;
		download.isDir = true;

		manager.download(download);

		assertTrue(Files.isDirectory(download.destination));
	}

	@Test
	public void closesDownload() throws Exception {
		nav().createFolder("syncRoot");
		download.source = Paths.get("/syncRoot");
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
		download.source = Paths.get("/testfile");
		download.destination = destination;
		download.mtime = lastUpload != null ? lastUpload.mtime + mtimeDiff : Files.getLastModifiedTime(destination).toMillis() + mtimeDiff;
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
		upload.destination = Paths.get("/testfile");
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
		upload.destination = Paths.get("/testfile");
		upload.mtime = lastUpload.mtime + OLDER;
		upload.type = DELETE;
		upload.isDir = false;
		manager.upload(upload);

		assertEquals(SKIPPED, upload.getState());
		assertRemoteExists("content", "testfile");
	}

	@Test
	public void skipsTempFilesWithStagingArea() throws Exception {
		manager.setStagingDelay(500L, TimeUnit.MILLISECONDS);

		Path path = tmpPath("file");
		File file = path.toFile();
		file.createNewFile();
		upload.source = path;
		upload.destination = Paths.get("/testfile");
		upload.mtime = file.lastModified();
		upload.type = CREATE;
		upload.transactionAge = 0L;
		upload.isDir = false;
		manager.addUpload(upload);
		managerNext();

		waitUntil(() -> upload.getState() == Transaction.STATE.WAITING);
		file.delete();
		upload.valid = false;

		waitUntil(() -> upload.getState() == Transaction.STATE.SKIPPED);
	}

	protected void managerNext() {
		new Thread(() -> {
			try {
				manager.next();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	@Test
	public void downloadsAreNotStaged() throws Exception {
		manager.setStagingDelay(2000L, TimeUnit.MILLISECONDS);

		download.source = Paths.get("/wayne");
		download.destination = tmpPath("wayne");
		download.mtime = 1000L;
		download.type = CREATE;
		download.isDir = false;
		manager.addDownload(download);
		managerNext();

		waitUntil(() -> upload.getState() == Transaction.STATE.FAILED);
	}

	private void assertRemoteExists(String content, String testfile) throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFile file = nav.getFile(testfile);
		assertEquals(content, IOUtils.toString(nav.download(file)));
	}
}
