package de.qabel.desktop.ui.remotefs;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.AccountingController;
import de.qabel.desktop.ui.accounting.AccountingView;
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
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RemoteFSControllerTest extends AbstractControllerTest {

	public static final String TEST_FOLDER = "testFolder";
	public static final String TEST_SUB_FOLDER = "subFolder";
	public static final String SUB_FILE = "tmp1.txt";
	public static final String TMP_DIR = "tmp";
	public static final String TEST_TMP_DIR = TMP_DIR + "/" + TEST_FOLDER;

	private BoxNavigation nav;
	private LocalWriteBackend localWrite;
	private RemoteFSController controller = new RemoteFSController();
	private UUID uuid = UUID.randomUUID();
	private final String prefix = UUID.randomUUID().toString();
	private File file;
	private File localStorageFile;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		file = File.createTempFile("File2", ".txt", new File(System.getProperty("java.io.tmpdir")));
		CryptoUtils utils = new CryptoUtils();
		byte[] deviceID = utils.getRandomBytes(16);
		QblECKeyPair keyPair = new QblECKeyPair();
		Path tempFolder = Files.createTempDirectory("");

		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		LocalReadBackend localRead = new LocalReadBackend(tempFolder);
		localWrite = new LocalWriteBackend(tempFolder);
		localStorageFile = new File(System.getProperty("java.io.tmpdir"));

		BoxVolume volume = new BoxVolume(
				localRead,
				localWrite,
				keyPair,
				deviceID,
				localStorageFile,
				prefix);

		String bucket = "qabel";
		volume.createIndex(bucket, prefix);
		nav = volume.navigate();
	}

	@After
	public void teadDown() throws Exception {
		localWrite.delete(localStorageFile.getName());
		FileUtils.deleteDirectory(new File(TEST_TMP_DIR));
		FileUtils.deleteDirectory(new File(TMP_DIR + "/" + controller.ROOT_FOLDER_NAME));
		FileUtils.deleteDirectory(new File(TMP_DIR + "/test"));
		File file = new File(TMP_DIR + "/" + "tmp1.txt");
		file.delete();
		super.tearDown();
	}

	@Test(timeout=1000L)
	public void showsEmptyStringOnUnknownColumn() {
		TreeItem rootNode = getRoot();
		assertThat(rootNode.getChildren().size(), is(0));
	}

	private LazyBoxFolderTreeItem getRoot() {
		LazyBoxFolderTreeItem rootNode = new LazyBoxFolderTreeItem(new BoxFolder("", "root", new byte[0]), nav);
		loadChildren(rootNode);
		return rootNode;
	}

	private ObservableList loadChildren(LazyBoxFolderTreeItem node) {
		ObservableList children = node.getChildren();
		while (node.isLoading()) {
			Thread.yield();
		}
		return children;
	}


	@Test
	public void injectlTest() {
		Locale.setDefault(new Locale("de", "DE"));
		RemoteFSView view = new RemoteFSView();
		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("http://localhost:9696","user","auth"));
		controller = (RemoteFSController) view.getPresenter();
		assertThat(controller.getRessource().getLocale().getCountry(), is("DE"));
		assertThat(controller.getRessource().getLocale().getLanguage(), is("de"));
	}

	@Test(timeout=1000L)
	public void testCalculateFolderStructure() {
		try {

			nav.createFolder("folder");
			nav.commit();

			TreeItem rootNode = getRoot();
			assertThat(rootNode.getChildren().size(), is(1));

		} catch (QblStorageException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout=1000L)
	public void testCalculateFileStructure() {
		try {

			nav.upload("File", file);
			nav.commit();

			TreeItem rootNode = getRoot();
			assertThat(rootNode.getChildren().size(), is(1));

		} catch (QblStorageException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout=1000L)
	public void testObserveFilesSubfolder() {
		try {
			BoxFolder folder = nav.createFolder("folder");
			BoxNavigation navFolder1 = nav.navigate(folder);
			navFolder1.upload("File", file);
			navFolder1.commit();

			TreeItem rootNode = getRoot();
			assertThat(rootNode.getChildren().size(), is(1));
			TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
			assertThat(loadChildren((LazyBoxFolderTreeItem) subnode).size(), is(1));

		} catch (QblStorageException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout=1000L)
	public void testObserveFoldersSubfolder() {
		try {

			BoxFolder folder = nav.createFolder("folder");
			nav.commit();

			BoxNavigation navFolder3 = nav.navigate(folder);
			navFolder3.createFolder("subFolder");
			navFolder3.commit();

			TreeItem rootNode = getRoot();
			assertThat(rootNode.getChildren().size(), is(1));
			TreeItem subnode = (TreeItem) rootNode.getChildren().get(0);
			assertThat(loadChildren((LazyBoxFolderTreeItem) subnode).size(), is(1));

		} catch (QblStorageException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout=1000L)
	public void testUploadedDirectoryInRootNode() throws QblStorageException, IOException {

		initTreeTable();
		assertThat(controller.nav.listFolders().size(), is(1));
		assertThat(controller.nav.listFiles().size(), is(0));

		BoxFolder folder = controller.nav.listFolders().get(0);
		BoxNavigation newNav = nav.navigate(folder);

		assertThat(newNav.listFiles().size(), is(1));
		assertThat(newNav.listFolders().size(), is(1));
	}

	@Test(timeout=1000L)
	public void testUploadedDirectoryInChildNode() throws QblStorageException, IOException {

		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);

		File subdir = new File(dir, "subFolder");
		subdir.mkdirs();
		controller.uploadedDirectory(dir, folder);

		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
		assertThat(newNav.listFolders().size(), is(1));

	}

	@Test(timeout=1000L)
	public void testCreateFolder() throws QblStorageException {

		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);
		controller.createFolder("NewFolder", folder);
		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
		assertThat(newNav.listFolders().size(), is(1));

	}


	@Test(timeout=1000L)
	public void TestUploadFiles() throws QblStorageException, IOException {
		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		BoxFolder folder = createBoxFolder(dir);
		File tmp = new File(dir, "tmp1.txt");
		tmp.createNewFile();
		controller.uploadFiles(tmp, folder);
		BoxNavigation newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(1));
		assertThat(newNav.listFolders().size(), is(0));

	}

	@Test(timeout=1000L)
	public void TestDontDeleteBoxFolder() throws QblStorageException, IOException {
		initTreeTable();
		BoxFolder folder = controller.nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.NO, folder, null);
		assertThat(controller.nav.listFolders().size(), is(1));
		assertThat(controller.nav.listFiles().size(), is(0));
	}

	@Test(timeout=1000L)
	public void TestDeleteBoxFolder() throws QblStorageException, IOException {
		initTreeTable();

		BoxFolder folder = controller.nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.OK, folder, null);
		assertThat(controller.nav.listFolders().size(), is(0));
		assertThat(controller.nav.listFiles().size(), is(0));
	}

	@Test(timeout=1000L)
	public void TestDeleteBoxFile() throws QblStorageException, IOException {
		initTreeTable();

		BoxFolder folder = nav.listFolders().get(0);

		BoxNavigation newNav = nav.navigate(folder);
		BoxObject boxFile = newNav.listFiles().get(0);
		controller.deleteBoxObject(ButtonType.OK, boxFile, folder);

		newNav = nav.navigate(folder);
		assertThat(newNav.listFiles().size(), is(0));
	}

	@Test(timeout=1000L)
	public void TestDeleteBoxFileFromRootNode() throws QblStorageException, IOException {
		initTreeTable();
		BoxFolder folder = nav.listFolders().get(0);
		controller.deleteBoxObject(ButtonType.OK, folder, null);
		assertThat(nav.listFiles().size(), is(0));
	}

	@Test(timeout=1000L)
	public void TestDownloadFolderFromRootNode() throws QblStorageException, IOException {
		initTreeTable();
		BoxFolder folder = nav.listFolders().get(0);
		controller.downloadBoxObjectRootNode(folder, null, TMP_DIR);

		File rootDir = new File(TMP_DIR + "/" + controller.ROOT_FOLDER_NAME);
		File dir = new File(TMP_DIR + "/" +
				controller.ROOT_FOLDER_NAME + "/" +
				TEST_FOLDER);
		File subDir = new File(TMP_DIR + "/" +
				controller.ROOT_FOLDER_NAME + "/" +
				TEST_FOLDER + "/" +
				TEST_SUB_FOLDER
		);

		assertThat(TEST_SUB_FOLDER, is(subDir.getName()));
		assertThat(controller.ROOT_FOLDER_NAME, is(rootDir.getName()));
		assertThat(TEST_FOLDER, is(dir.getName()));
	}

	@Test(timeout=1000L)
	public void TestDownloadFolderFromNode() throws QblStorageException, IOException {
		initTreeTable();
		BoxFolder root = nav.listFolders().get(0);
		BoxNavigation newNav = nav.navigate(root);
		BoxFolder folder = newNav.listFolders().get(0);
		controller.downloadBoxObject(folder, root, TEST_TMP_DIR);

		File dir = new File(TEST_TMP_DIR);
		File subDir = new File(TEST_TMP_DIR
				+ "/" +
				TEST_SUB_FOLDER
		);

		assertThat(TEST_FOLDER, is(dir.getName()));
		assertThat(TEST_SUB_FOLDER, is(subDir.getName()));
	}

	@Test(timeout=1000L)
	public void TestDownloadFileFromRootNode() throws QblStorageException, IOException {
		controller.nav = nav;

		File testDir = new File(TEST_TMP_DIR);
		testDir.mkdirs();
		File tmpFile = new File(testDir, SUB_FILE);
		tmpFile.createNewFile();
		controller.uploadFiles(tmpFile, null);
		BoxFile file = nav.listFiles().get(0);

		controller.downloadBoxObject(file, null, TMP_DIR);
		File testFile = new File(TMP_DIR + "/" + SUB_FILE);

		assertThat(SUB_FILE, is(testFile.getName()));


	}

	@Test(timeout=1000L)
	public void TestDownloadFileFromSubNode() throws QblStorageException, IOException {
		initTreeTable();

		BoxFolder root = nav.listFolders().get(0);
		BoxNavigation newNav = nav.navigate(root);
		BoxFile file = newNav.listFiles().get(0);

		controller.downloadBoxObject(file, root, TMP_DIR);
		File testFile = new File(TMP_DIR + "/" + SUB_FILE);
		assertThat(SUB_FILE, is(testFile.getName()));
	}

	private void initTreeTable() throws IOException {
		File dir = craeteFileAndFolder();
		controller.nav = nav;
		controller.uploadedDirectory(dir, null);
	}

	private BoxFolder createBoxFolder(File dir) throws QblStorageException {
		controller.nav = nav;
		controller.uploadedDirectory(dir, null);
		return controller.nav.listFolders().get(0);
	}

	private File craeteFileAndFolder() throws IOException {
		File dir = new File(TEST_TMP_DIR);
		dir.mkdirs();
		File tmp = new File(dir, SUB_FILE);
		tmp.createNewFile();
		File subdir = new File(dir, TEST_SUB_FOLDER);
		subdir.mkdirs();
		return dir;
	}
}
