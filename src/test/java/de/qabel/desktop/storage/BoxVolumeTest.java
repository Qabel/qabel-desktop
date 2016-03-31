package de.qabel.desktop.storage;


import de.qabel.core.config.Contact;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNameConflict;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.meanbean.util.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public abstract class BoxVolumeTest {
	private static final Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getSimpleName());
	private final String DEFAULT_UPLOAD_FILENAME = "foobar";

	protected BoxVolume volume;
	protected BoxVolume volume2;
	protected byte[] deviceID;
	protected byte[] deviceID2;
	protected QblECKeyPair keyPair;
	protected final String bucket = "qabel";
	protected String prefix = UUID.randomUUID().toString();
	private final String testFileName = "src/test/java/de/qabel/desktop/storage/testFile.txt";
	protected Contact contact;

	@Before
	public void setUp() throws IOException, QblStorageException {
		CryptoUtils utils = new CryptoUtils();
		deviceID = utils.getRandomBytes(16);
		deviceID2 = utils.getRandomBytes(16);

		keyPair = new QblECKeyPair();
		contact = new Contact("contact", new LinkedList<>(), new QblECKeyPair().getPub());

		setUpVolume();

		volume.createIndex(bucket, prefix);
	}

	protected abstract StorageReadBackend getReadBackend();

	protected abstract void setUpVolume() throws IOException;

	@After
	public void cleanUp() throws IOException {
		cleanVolume();
	}

	protected abstract void cleanVolume() throws IOException;

	@Test
	public void testCreateIndex() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		assertThat(nav.listFiles().size(), is(0));
	}

	@Test
	public void testUploadFile() throws QblStorageException, IOException {
		uploadFile(volume.navigate());
	}

	@Test
	public void modifiedStateIsClearedOnCommit() throws Exception {
		IndexNavigation nav = volume.navigate();
		nav.setAutocommit(false);
		uploadFile(nav);
		assertFalse(nav.isUnmodified());
		nav.commit();
		assertTrue(nav.isUnmodified());
	}

	@Test(expected = QblStorageNotFound.class)
	public void testDeleteFile() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFile boxFile = uploadFile(nav);
		nav.delete(boxFile);
		nav.download(boxFile);
	}

	private BoxFile uploadFile(BoxNavigation nav) throws QblStorageException, IOException {
		String filename = DEFAULT_UPLOAD_FILENAME;
		return uploadFile(nav, filename);
	}

	private BoxFile uploadFile(BoxNavigation nav, String filename) throws QblStorageException, IOException {
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload(filename, file);
		BoxNavigation nav_new = volume.navigate();
		checkFile(boxFile, nav_new);
		return boxFile;
	}

	private void checkFile(BoxFile boxFile, BoxNavigation nav) throws QblStorageException, IOException {
		InputStream dlStream = nav.download(boxFile);
		assertNotNull("Download stream is null", dlStream);
		byte[] dl = IOUtils.toByteArray(dlStream);
		File file = new File(testFileName);
		assertThat(dl, is(Files.readAllBytes(file.toPath())));
	}

	@Test
	public void testCreateFolder() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFolder boxFolder = nav.createFolder("foobdir");

		BoxNavigation folder = nav.navigate(boxFolder);
		assertNotNull(folder);
		BoxFile boxFile = uploadFile(folder);

		BoxNavigation folder_new = nav.navigate(boxFolder);
		checkFile(boxFile, folder_new);

		BoxNavigation nav_new = volume.navigate();
		List<BoxFolder> folders = nav_new.listFolders();
		assertThat(folders.size(), is(1));
		assertThat(boxFolder, equalTo(folders.get(0)));
	}

	@Test
	public void testDeleteFolder() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFolder boxFolder = nav.createFolder("foobdir");

		BoxNavigation folder = nav.navigate(boxFolder);
		BoxFile boxFile = uploadFile(folder);
		BoxFolder subfolder = folder.createFolder("subfolder");

		nav.delete(boxFolder);
		BoxNavigation nav_after = volume.navigate();
		assertThat(nav_after.listFolders().isEmpty(), is(true));
		checkDeleted(boxFolder, subfolder, boxFile, nav_after);
	}

	private void checkDeleted(BoxFolder boxFolder, BoxFolder subfolder, BoxFile boxFile, BoxNavigation nav) throws QblStorageException {
		try {
			nav.download(boxFile);
			AssertionUtils.fail("Could download file in deleted folder");
		} catch (QblStorageNotFound e) {
		}
		try {
			nav.navigate(boxFolder);
			AssertionUtils.fail("Could navigate to deleted folder");
		} catch (QblStorageNotFound e) {
		}
		try {
			nav.navigate(subfolder);
			AssertionUtils.fail("Could navigate to deleted subfolder");
		} catch (QblStorageNotFound e) {
		}
	}

	@Test(expected = QblStorageNameConflict.class)
	public void testOverwriteFileError() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		uploadFile(nav);
		uploadFile(nav);
	}

	@Test(expected = QblStorageNotFound.class)
	public void testOverwriteFileNotFound() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		nav.overwrite(DEFAULT_UPLOAD_FILENAME, file);
	}

	@Test
	public void testOverwriteFile() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		nav.upload(DEFAULT_UPLOAD_FILENAME, file);
		nav.overwrite(DEFAULT_UPLOAD_FILENAME, file);
		assertThat(nav.listFiles().size(), is(1));
	}

	@Test
	public void testConflictFileUpdate() throws QblStorageException, IOException {
		BoxNavigation nav = setupConflictNav1();
		BoxNavigation nav2 = setupConflictNav2();
		File file = new File(testFileName);
		nav.upload(DEFAULT_UPLOAD_FILENAME, file);
		nav2.upload(DEFAULT_UPLOAD_FILENAME, file);
		nav2.commit();
		nav.commit();
		assertThat(nav.listFiles().size(), is(2));
	}

	@Test
	public void testFoldersAreMergedOnConflict() throws Exception {
		BoxNavigation nav = setupConflictNav1();
		BoxNavigation nav2 = setupConflictNav2();

		nav.createFolder("folder1");
		nav2.createFolder("folder2");

		nav2.commit();
		nav.commit();
		assertThat(nav.listFolders().size(), is(2));
	}

	private BoxNavigation setupConflictNav1() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		nav.setAutocommit(false);
		return nav;
	}

	@Test
	public void testDeletedFoldersAreMergedOnConflict() throws Exception {
		BoxNavigation nav = setupConflictNav1();
		BoxFolder folder1 = nav.createFolder("folder1");
		nav.commit();

		BoxNavigation nav2 = setupConflictNav2();
		BoxFolder folder2 = nav2.createFolder("folder2");
		nav2.commit();
		nav = setupConflictNav1();

		nav2.delete(folder2);
		nav.delete(folder1);
		nav2.commit();
		nav.commit();

		nav.setMetadata(nav.reloadMetadata());
		assertFalse(nav.hasFolder("folder2"));
		assertFalse(nav.hasFolder("folder1"));
	}

	@Test
	public void testDeletedFilesAreMergedOnConflict() throws Exception {
		BoxNavigation nav = setupConflictNav1();
		BoxFile file1 = uploadFile(nav, "file1");
		nav.commit();
		getReadBackend().download("blocks/" + file1.getBlock()).close();

		BoxNavigation nav2 = setupConflictNav2();
		BoxFile file2 = uploadFile(nav2, "file2");
		nav2.commit();
		getReadBackend().download("blocks/" + file2.getBlock()).close();
		nav = setupConflictNav1();

		nav2.delete(file2);
		nav.delete(file1);
		nav2.commit();
		nav.commit();

		nav.setMetadata(nav.reloadMetadata());
		assertFalse(nav.hasFile("file1"));
		assertFalse(nav.hasFile("file2"));

		assertFileBlockDeleted(file1);
		assertFileBlockDeleted(file2);
	}

	private void assertFileBlockDeleted(BoxFile file2) throws IOException, QblStorageException {
		try {
			getReadBackend().download("blocks/" + file2.getBlock()).close();
			fail("block of file " + file2.getName() + " was not deleted");
		} catch (QblStorageNotFound ignored) {
		}
	}

	@Test(expected = QblStorageNameConflict.class)
	public void testFileNameConflict() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		nav.createFolder(DEFAULT_UPLOAD_FILENAME);
		nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));
	}

	@Test(expected = QblStorageNameConflict.class)
	public void testFolderNameConflict() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));
		nav.createFolder(DEFAULT_UPLOAD_FILENAME);
	}

	@Test
	public void testNameConflictOnDifferentClients() throws QblStorageException, IOException {
		BoxNavigation nav = setupConflictNav1();
		BoxNavigation nav2 = setupConflictNav2();
		File file = new File(testFileName);
		nav.upload(DEFAULT_UPLOAD_FILENAME, file);
		nav2.createFolder(DEFAULT_UPLOAD_FILENAME);
		nav2.commit();
		nav.commit();
		assertThat(nav.listFiles().size(), is(1));
		assertThat(nav.listFolders().size(), is(1));
		assertThat(nav.listFiles().get(0).name, startsWith("foobar_conflict"));
	}

	@Test
	public void testAddsShareToIndexWhenShareIsCreated() throws Exception {
		IndexNavigation index = volume.navigate();
		index.createFolder("subfolder");
		BoxNavigation nav = index.navigate("subfolder");
		BoxFile file = nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));

		nav.share(keyPair.getPub(), file, "receiverId");

		assertThat(index.listShares().size(), is(1));
	}

	/**
	 * Currently a folder with a name conflict just disappears and all is lost.
	 */
	@Test
	@Ignore
	public void testFolderNameConflictOnDifferentClients() throws QblStorageException, IOException {
		BoxNavigation nav = setupConflictNav1();
		BoxNavigation nav2 = setupConflictNav2();
		File file = new File(testFileName);
		nav.createFolder(DEFAULT_UPLOAD_FILENAME);
		nav2.upload(DEFAULT_UPLOAD_FILENAME, file);
		nav2.commit();
		nav.commit();
		assertThat(nav.listFiles().size(), is(1));
		assertThat(nav.listFolders().size(), is(1));
		assertThat(nav.listFiles().get(0).name, startsWith("foobar_conflict"));
	}

	private BoxNavigation setupConflictNav2() throws QblStorageException {
		BoxNavigation nav2 = volume2.navigate();
		nav2.setAutocommit(false);
		return nav2;
	}

	@Test
	public void testShare() throws Exception {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("file1", file);
		nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());

		BoxNavigation nav2 = volume2.navigate();
		BoxFile boxFile2 = nav2.getFile("file1");
		assertNotNull(boxFile2.getMeta());
		assertNotNull(boxFile2.getMetakey());
		assertEquals(boxFile.getMeta(), boxFile2.getMeta());
		assertTrue(Arrays.equals(boxFile.getMetakey(), boxFile2.getMetakey()));
		assertTrue(boxFile2.isShared());
		assertEquals(1, nav2.getSharesOf(boxFile2).size());
		assertEquals(contact.getKeyIdentifier(), nav2.getSharesOf(boxFile2).get(0).getRecipient());
		assertEquals(boxFile.getRef(), nav2.getSharesOf(boxFile2).get(0).getRef());
	}

	@Test
	public void testShareUpdate() throws Exception {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("file1", file);
		nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());

		BoxFile updatedBoxFile = nav.overwrite("file1", file);
		assertEquals(boxFile.getMeta(), updatedBoxFile.getMeta());
		assertArrayEquals(boxFile.getMetakey(), updatedBoxFile.getMetakey());

		BoxNavigation nav2 = volume2.navigate();
		BoxFile boxFile2 = nav2.getFile("file1");
		assertNotNull(boxFile2.getMeta());
		assertNotNull(boxFile2.getMetakey());
		assertEquals(boxFile.getMeta(), boxFile2.getMeta());
		assertTrue(Arrays.equals(boxFile.getMetakey(), boxFile2.getMetakey()));
		assertTrue(boxFile2.isShared());
		assertEquals(1, nav2.getSharesOf(boxFile2).size());
		assertEquals(contact.getKeyIdentifier(), nav2.getSharesOf(boxFile2).get(0).getRecipient());
		assertEquals(updatedBoxFile.getRef(), nav2.getSharesOf(boxFile2).get(0).getRef());

		FileMetadata fm = nav2.getFileMetadata(boxFile);
		BoxExternalFile externalFile = fm.getFile();
		assertEquals("the file metadata have not been updated", updatedBoxFile.getBlock(), externalFile.getBlock());
	}

	private File download(InputStream in) throws IOException {
		Path path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "tmpdownload", "");
		Files.write(path, IOUtils.toByteArray(in));
		return path.toFile();
	}

	@Test
	public void testUnshare() throws Exception {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("file1", file);
		nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());
		nav.unshare(boxFile);

		BoxNavigation nav2 = volume2.navigate();
		BoxFile boxFile2 = nav2.getFile("file1");
		assertNull(boxFile2.getMeta());
		assertNull(boxFile2.getMetakey());
		assertFalse(boxFile2.isShared());
		assertEquals(0, nav2.getSharesOf(boxFile2).size());
	}

	@Test
	public void deleteCleansShares() throws Exception {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("file1", file);
		nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());
		String prefix = boxFile.getPrefix();
		String meta = boxFile.getMeta();
		byte[] metakey = boxFile.getMetakey();
		assertTrue(blockExists(meta));
		assertFalse(nav.getSharesOf(boxFile).isEmpty());

		nav.delete(boxFile);
		assertNull(boxFile.getMeta());
		assertNull(boxFile.getMetakey());

		// file metadata has been deleted
		assertFalse(blockExists(meta));

		// share has been removed from index
		boxFile.setMeta(meta);
		boxFile.setMetakey(metakey);
		assertTrue(nav.getSharesOf(boxFile).isEmpty());
	}

	protected boolean blockExists(String meta) throws QblStorageException {
		try {
			getReadBackend().download(meta);
			return true;
		} catch (QblStorageNotFound e) {
			return false;
		}
	}
}
