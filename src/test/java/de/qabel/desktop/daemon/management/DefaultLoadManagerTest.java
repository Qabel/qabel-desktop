package de.qabel.desktop.daemon.management;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.config.factory.LocalBoxVolumeFactory;
import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
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
import java.util.List;

import static org.junit.Assert.*;

public class DefaultLoadManagerTest extends AbstractSyncTest {
	private BoxVolume volume;
	private UploadStub upload;
	private DefaultLoadManager manager;

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
			Identity identity = null;
				identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).build();
			volume = new LocalBoxVolumeFactory(tmpDir, "abc").getVolume(account, identity);
			volume.createIndex("??");

			upload = new UploadStub();
			upload.volume = volume;
			manager = new DefaultLoadManager();
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
		Upload upload = new FakeUpload();
		manager.addUpload(upload);
		assertEquals(1, manager.getUploads().size());
		assertSame(upload, manager.getUploads().get(0));
	}

	@Test
	public void createsRootDirectory() throws QblStorageException {
		upload.source = tmpPath("/syncRoot");
		upload.destination = Paths.get("syncRoot");
		upload.source.toFile().mkdirs();

		manager.upload(upload);

		List<BoxFolder> folders = nav().listFolders();
		assertEquals(1, folders.size());
		assertEquals("syncRoot", folders.get(0).name);
	}

	@Test
	public void createsSubDirectories() throws Exception {
		nav().createFolder("syncRoot");
		upload.source = tmpPath("/syncRoot/subdir");
		upload.source.toFile().mkdirs();
		upload.destination = Paths.get("/syncRoot/targetSubdir");

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
		upload.type = Upload.TYPE.DELETE;

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
		upload.type = Upload.TYPE.DELETE;
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
		manager.upload(upload);

		upload.type = Upload.TYPE.UPDATE;
		write("content2", upload.source);
		manager.upload(upload);

		BoxNavigation syncRoot = nav().navigate("syncRoot");
		List<BoxFile> files = syncRoot.listFiles();
		assertEquals(1, files.size());
		BoxFile boxFile = files.get(0);
		assertEquals("targetFile", boxFile.name);
		assertEquals("content2", IOUtils.toString(syncRoot.download(boxFile)));
	}

	private void write(String content, Path file) throws IOException {
		Files.write(file, content.getBytes());
	}
}
