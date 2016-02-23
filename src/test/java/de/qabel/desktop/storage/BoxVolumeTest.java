package de.qabel.desktop.storage;


import de.qabel.core.config.Contact;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public abstract class BoxVolumeTest {
	private static final Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getSimpleName());

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

	@Test(expected = QblStorageNotFound.class)
	public void testDeleteFile() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFile boxFile = uploadFile(nav);
		nav.delete(boxFile);
		nav.download(boxFile);
	}

	private BoxFile uploadFile(BoxNavigation nav) throws QblStorageException, IOException {
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("foobar", file);
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
		nav.overwrite("foobar", file);
	}

	@Test
	public void testOverwriteFile() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		nav.upload("foobar", file);
		nav.overwrite("foobar", file);
		assertThat(nav.listFiles().size(), is(1));
	}

	@Test
	public void testConflictFileUpdate() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		nav.setAutocommit(false);
		BoxNavigation nav2 = volume2.navigate();
		nav2.setAutocommit(false);
		File file = new File(testFileName);
		nav.upload("foobar", file);
		nav2.upload("foobar", file);
		nav2.commit();
		nav.commit();
		assertThat(nav.listFiles().size(), is(2));
	}

	@Test(expected = QblStorageNameConflict.class)
	public void testFileNameConflict() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		nav.createFolder("foobar");
		nav.upload("foobar", new File(testFileName));
	}

	@Test(expected = QblStorageNameConflict.class)
	public void testFolderNameConflict() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		nav.upload("foobar", new File(testFileName));
		nav.createFolder("foobar");
	}

	@Test
	public void testNameConflictOnDifferentClients() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		nav.setAutocommit(false);
		BoxNavigation nav2 = volume2.navigate();
		nav2.setAutocommit(false);
		File file = new File(testFileName);
		nav.upload("foobar", file);
		nav2.createFolder("foobar");
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
		BoxFile file = nav.upload("foobar", new File(testFileName));

		nav.share(keyPair.getPub(), file, "receiverId");

		assertThat(index.listShares().size(), is(1));
	}

	/**
	 * Currently a folder with a name conflict just disappears and all is lost.
	 */
	@Test
	@Ignore
	public void testFolderNameConflictOnDifferentClients() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		nav.setAutocommit(false);
		BoxNavigation nav2 = volume2.navigate();
		nav2.setAutocommit(false);
		File file = new File(testFileName);
		nav.createFolder("foobar");
		nav2.upload("foobar", file);
		nav2.commit();
		nav.commit();
		assertThat(nav.listFiles().size(), is(1));
		assertThat(nav.listFolders().size(), is(1));
		assertThat(nav.listFiles().get(0).name, startsWith("foobar_conflict"));
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
	public void deleteCleansShares() throws Exception {BoxNavigation nav = volume.navigate();
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
