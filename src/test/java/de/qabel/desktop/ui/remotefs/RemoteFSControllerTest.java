package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class RemoteFSControllerTest extends AbstractControllerTest {

	public static final String TEST_FOLDER = "testFolder";
	public static final String TEST_SUB_FOLDER = "subFolder";
	public static final String SUB_FILE = "tmp1.txt";
	public static final String TMP_DIR = "/tmp/tmpQbl";
	public static final String TEST_TMP_DIR = TMP_DIR + "/" + TEST_FOLDER;

	private BoxNavigation nav;
	private LocalWriteBackend localWrite;
	private RemoteFSController controller;
	private UUID uuid = UUID.randomUUID();
	private final String prefix = UUID.randomUUID().toString();
	private File file;
	private File localStorageFile;
	private Path tempFolder;
	private RemoteFSView view;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		file = File.createTempFile("File2", ".txt", new File(System.getProperty("java.io.tmpdir")));
		CryptoUtils utils = new CryptoUtils();
		byte[] deviceID = utils.getRandomBytes(16);
		QblECKeyPair keyPair = new QblECKeyPair();
		tempFolder = Files.createTempDirectory("");

		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		LocalReadBackend localRead = new LocalReadBackend(tempFolder);
		localWrite = new LocalWriteBackend(tempFolder);
		localStorageFile = new File(System.getProperty("java.io.tmpdir"));

		BoxVolume volume = new CachedBoxVolume(
				localRead,
				localWrite,
				keyPair,
				deviceID,
				localStorageFile,
				prefix);
		boxVolumeFactory.boxVolume = volume;

		String bucket = "qabel";
		volume.createIndex(bucket, prefix);
		nav = volume.navigate();

		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("http://localhost:9696","user","auth"));

		Locale.setDefault(new Locale("de", "DE"));
		view = new RemoteFSView();
		RemoteFSView view = new RemoteFSView();
		controller = (RemoteFSController) view.getPresenter();
	}

	@After
	public void tearDown() throws Exception {
		localWrite.delete(localStorageFile.getName());
		FileUtils.deleteDirectory(new File(TEST_TMP_DIR));
		FileUtils.deleteDirectory(new File(TMP_DIR + "/" + controller.ROOT_FOLDER_NAME));
		FileUtils.deleteDirectory(new File(TMP_DIR + "/test"));
		File file = new File(TMP_DIR + "/" + "tmp1.txt");
		file.delete();

		FileUtils.deleteDirectory(tempFolder.toFile());
		super.tearDown();
	}

	@Test(timeout=10000L)
	public void showsEmptyStringOnUnknownColumn() {
		TreeItem rootNode = getRoot();
		assertThat(rootNode.getChildren().size(), is(0));
	}

	private FolderTreeItem getRoot() {
		FolderTreeItem rootNode = new FolderTreeItem(new BoxFolder("", "root", new byte[0]), nav);
		loadChildren(rootNode);
		return rootNode;
	}

	private ObservableList loadChildren(FolderTreeItem node) {
		ObservableList children = node.getChildren();
		while (node.isLoading()) {
			Thread.yield();
		}
		return children;
	}

	@Test
	public void injectlTest() {
		assertThat(controller.getRessource().getLocale().getCountry(), is("DE"));
		assertThat(controller.getRessource().getLocale().getLanguage(), is("de"));
		assertThat(controller.loadManager, is(notNullValue()));
	}

	@Test(timeout=10000L)
	public void testCalculateFolderStructure() throws Exception {
		nav.createFolder("folder");
		nav.commit();

		TreeItem rootNode = getRoot();
		waitUntil(() -> rootNode.getChildren().size() == 1);
	}

	@Test(timeout=10000L)
	public void testCalculateFileStructure() {
		try {

			nav.upload("File", file);
			nav.commit();

			TreeItem rootNode = getRoot();
			waitUntil(() -> rootNode.getChildren().size() == 1);

		} catch (QblStorageException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout=10000L)
	public void testObserveFilesSubfolder() throws Exception {
			BoxFolder folder = nav.createFolder("folder");
			BoxNavigation navFolder1 = nav.navigate(folder);
			navFolder1.upload("File", file);
			navFolder1.commit();

			TreeItem rootNode = getRoot();
			waitUntil(() -> rootNode.getChildren().size() == 1);
			TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
			waitUntil(() -> loadChildren((FolderTreeItem) subnode).size() == 1);
	}

	@Test(timeout=10000L)
	public void testObserveFoldersSubfolder() throws Exception {
		BoxFolder folder = nav.createFolder("folder");
		nav.commit();

		BoxNavigation navFolder3 = nav.navigate(folder);
		navFolder3.createFolder("subFolder");
		navFolder3.commit();

		TreeItem rootNode = getRoot();
		waitUntil(() -> rootNode.getChildren().size() == 1);
		TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
		waitUntil(() -> loadChildren((FolderTreeItem) subnode).size() == 1);
	}

	@Test(timeout=10000L)
	public void testUploadedDirectoryInChildNode() throws Exception {
		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);

		File subdir = new File(dir, "subFolder");
		new File(subdir, "subsubFolder").mkdirs();
		new File(subdir, "subsubFile").createNewFile();
		controller.uploadDirectory(dir.toPath(), Paths.get("/", TEST_FOLDER));
		executeTransactions();

		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
		assertThat(newNav.listFolders().size(), is(1));
		assertThat(newNav.navigate("subFolder").listFolders().get(0).getName(), is("subsubFolder"));
		assertThat(newNav.navigate("subFolder").listFiles().get(0).getName(), is("subsubFile"));
	}

	@Test(timeout=10000L)
	public void testCreateFolder() throws Exception {

		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);
		controller.createFolder(Paths.get("/", TEST_FOLDER, dir.getName()));
		executeTransactions();

		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
		assertThat(newNav.listFolders().size(), is(1));

	}

	@Test(timeout=10000L)
	public void testUploadFiles() throws QblStorageException, IOException, InterruptedException {
		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);
		File tmp = new File(dir, "tmp1.txt");
		tmp.createNewFile();
		controller.upload(tmp.toPath(), Paths.get("/", TEST_FOLDER, "/", "tmp1.txt"));

		assertEquals(1, loadManager.getTransactions().size());
		executeTransactions();
		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(1));
		assertThat(newNav.listFolders().size(), is(0));

	}

	private void executeTransactions() throws InterruptedException {
		while (!loadManager.getTransactions().isEmpty()) {
			loadManager.next();
		}
	}

	@Test(timeout=10000L)
	public void testDontDeleteBoxFolder() throws Exception {
		initTreeTable();
		BoxFolder folder = controller.nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.NO, Paths.get("/", TEST_FOLDER), folder);
		executeTransactions();

		assertThat(controller.nav.listFolders().size(), is(1));
		assertThat(controller.nav.listFiles().size(), is(0));
	}

	@Test(timeout=10000L)
	public void TestDeleteBoxFolder() throws QblStorageException, IOException, InterruptedException {
		initTreeTable();

		BoxFolder folder = nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.OK, Paths.get("/", TEST_FOLDER), folder);
		executeTransactions();

		assertThat(controller.nav.listFolders().size(), is(0));
		assertThat(controller.nav.listFiles().size(), is(0));
	}

	@Test(timeout=10000L)
	public void testDeleteBoxFile() throws Exception {
		initTreeTable();

		BoxFolder folder = nav.listFolders().get(0);
		BoxFile file = nav.navigate(folder).listFiles().get(0);

		controller.deleteBoxObject(ButtonType.OK, Paths.get("/", TEST_FOLDER, SUB_FILE), file);
		executeTransactions();

		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
	}

	@Test(timeout=10000L)
	public void TestDeleteBoxFileFromRootNode() throws QblStorageException, IOException {
		initTreeTable();
		BoxFolder folder = nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.OK, Paths.get("/", TEST_FOLDER), folder);
		assertThat(nav.listFiles().size(), is(0));
	}

	@Test(timeout=10000L)
	public void testDownloadFolderFromRootNode() throws Exception {
		BoxFolder folder = nav.createFolder("folder");
		Path targetDir = Paths.get(TMP_DIR, "folder");
		controller.downloadBoxObject(folder, nav.navigate("folder"), Paths.get("/folder"), targetDir);
		executeTransactions();

		assertTrue("folder was not downloaded", Files.isDirectory(targetDir));
	}

	@Test
	public void testDownloadFolderFromNode() throws Exception {
		BoxFolder node = nav.createFolder(TEST_FOLDER);
		BoxNavigation nodeNav = nav.navigate(node);
		BoxFolder folder = nodeNav.createFolder(TEST_SUB_FOLDER);
		nodeNav.navigate(folder).upload(SUB_FILE, file);
		Path targetFolder = tempFolder.resolve(folder.getName());
		controller.downloadBoxObject(folder, nodeNav.navigate(folder), Paths.get("/", node.getName(), folder.getName()), tempFolder);
		executeTransactions();

		assertTrue(Files.isDirectory(targetFolder));
		Path filePath = targetFolder.resolve(SUB_FILE);
		assertTrue(filePath.toString() + " has not been downloaded", Files.exists(filePath));
	}

	@Test
	public void testRecursiveDownload() throws Exception {
		BoxFolder node = nav.createFolder("folder");
		nav.navigate("folder").createFolder("subfolder");
		Path targetFolder = tempFolder.resolve("folder");
		controller.downloadBoxObject(node, nav.navigate("folder"), Paths.get("/folder"), tempFolder);
		executeTransactions();

		assertTrue(Files.isDirectory(targetFolder.resolve("subfolder")));
	}

	@Test
	public void testDownloadFileFromRootNode() throws Exception {
		controller.nav = nav;

		File testDir = new File(TEST_TMP_DIR);
		testDir.mkdirs();
		File tmpFile = new File(testDir, SUB_FILE);
		tmpFile.createNewFile();
		Path remotePath = Paths.get("/" + SUB_FILE);
		controller.upload(tmpFile.toPath(), remotePath);
		executeTransactions();

		BoxFile file = nav.listFiles().get(0);

		controller.downloadBoxObject(file, nav, remotePath, Paths.get(TMP_DIR));
		executeTransactions();
		File testFile = new File(TMP_DIR + "/" + SUB_FILE);

		assertTrue(testFile.exists());
	}

	@Test(timeout=10000L)
	public void testDownloadFileFromSubNode() throws Exception {
		initTreeTable();

		BoxFolder root = nav.listFolders().get(0);
		BoxNavigation newNav = nav.navigate(root);
		BoxFile file = newNav.listFiles().get(0);

		controller.downloadBoxObject(file, newNav, Paths.get("/", TEST_FOLDER, "/", SUB_FILE), Paths.get(TMP_DIR));
		executeTransactions();
		File testFile = new File(TMP_DIR + "/" + SUB_FILE);
		assertTrue(testFile.exists());
	}

	private void initTreeTable() throws IOException, QblStorageException {
		createFileAndFolder();
		BoxFolder folder = nav.createFolder(TEST_FOLDER);
		nav.navigate(folder).upload(SUB_FILE, new File(TEST_TMP_DIR, SUB_FILE));
		nav.navigate(folder).createFolder(TEST_SUB_FOLDER);
	}

	private BoxFolder createBoxFolder(File dir) throws QblStorageException {
		controller.nav = nav;
		nav.createFolder(dir.getName());
		return controller.nav.listFolders().get(0);
	}

	private File createFileAndFolder() throws IOException {
		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		File tmp = new File(dir, SUB_FILE);
		tmp.createNewFile();
		File subdir = new File(dir, TEST_SUB_FOLDER);
		subdir.mkdirs();
		return dir;
	}
}
