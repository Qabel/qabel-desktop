package de.qabel.desktop.ui.sync;

import com.airhacks.afterburner.views.FXMLView;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.config.factory.CachedBoxVolumeFactory;
import de.qabel.desktop.config.factory.LocalBoxVolumeFactory;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.ui.AbstractStepdefs;
import de.qabel.desktop.ui.sync.item.SyncItemPage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncStepdefs extends AbstractStepdefs<SyncController> {
    private SyncPage page;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Path tmpDir;
    private BoxVolumeFactory boxVolumeFactory;
    private Path localPath;
    private Path remotePath;
    private BoxVolume volume;
    private String syncName;
    private SyncDaemon syncDaemon;

    @Override
    @Before("@sync")
    public void setUp() throws Exception {
        super.setUp();

        page = new SyncPage(baseFXRobot, robot, controller);
        tmpDir = Files.createTempDirectory("qabelTest").toAbsolutePath();
        boxVolumeFactory = new CachedBoxVolumeFactory(new LocalBoxVolumeFactory(tmpDir, "testdevice", "testprefix"));
        executor.submit(transferManager);
        BoxSyncRepository syncRepo = boxSyncRepository;
        ObservableList<BoxSyncConfig> configs = FXCollections.observableList(syncRepo.findAll());
        syncRepo.onAdd(configs::add);
        syncDaemon = new SyncDaemon(configs, new DefaultSyncerFactory(boxVolumeFactory, transferManager));
        executor.submit(syncDaemon);
        diContainer.put("syncDaemon", syncDaemon);
    }

    @Override
    protected FXMLView getView() {
        return new SyncView();
    }

    @Override
    @After("@sync")
    public void tearDown() throws Exception {
        executor.shutdownNow();
        try {
            if (Files.isDirectory(tmpDir)) {
                FileUtils.deleteDirectory(tmpDir.toFile());
            }
        } catch (IOException e) {
            if (Files.isDirectory(tmpDir)) {
                FileUtils.deleteDirectory(tmpDir.toFile());
            }
        }
        super.tearDown();
    }

    private Path toTmpPath(String folder) throws Exception {
        return tmpDir.resolve(folder);
    }

    @Given("^a sync (.+) from '(.+)' to '(/?.+)'$")
    public void aSyncAFromLocalFolderToRemoteFolder(String name, String from, String to) throws Throwable {
        localPath = toTmpPath(from);
        remotePath = BoxFileSystem.get(to);
        syncName = name;
        BoxSyncConfig syncConfig = new DefaultBoxSyncConfig(
            syncName,
            localPath,
            remotePath,
            identity,
            account,
            new InMemorySyncIndexFactory()
        );
        volume = boxVolumeFactory.getVolume(account, identity);
        boxSyncRepository.save(syncConfig);
        waitUntil(() -> syncConfig.getSyncer() != null && syncConfig.getSyncer().isSynced(), 30000L);
    }

    @And("^a synced file '(.+)'$")
    public void aSyncedFileTestfile(String filename) throws Throwable {
        Files.write(localPath.resolve(filename), ".".getBytes());
        waitUntil(() -> {
            BoxNavigation navigation = volume.navigate();
            for (int i = 0; i < remotePath.getNameCount(); i++) {
                String folder = remotePath.getName(i).getFileName().toString();
                navigation = navigation.navigate(folder);
            }
            return navigation.hasFile(filename);
        }, 10000L);
    }

    @When("^I change the local folder to '(.+)'$")
    public void iChangeTheLocalFolder(String newPath) throws Throwable {
        SyncItemPage itemPage = page.getSync(syncName);
        itemPage.edit().enterLocalPath(toTmpPath(newPath).toString()).save();
    }

    @Then("^'(.+)' exists in local '(.+)'$")
    public void fileExistsInLocal(String filename, String localPath) throws Throwable {
        Path targetFile = toTmpPath(localPath).resolve(filename);
        waitUntil(() -> Files.exists(targetFile), 10000L, () -> "file not found: " + targetFile);
    }

    @And("^'(.+)' exists in remote '(.+)'$")
    public void fileExistsInRemote(String filename, String remotePath) throws Throwable {
        Path remote = BoxFileSystem.get("/", remotePath);
        final BoxNavigation remoteNavigation = navigate(remote);
        waitUntil(() -> remoteNavigation.hasFile(filename));
    }

    private BoxNavigation navigate(Path remote) throws QblStorageException {
        BoxNavigation navigation = volume.navigate();
        for (int i = 0; i < remote.getNameCount(); i++) {
            BoxNavigation current = navigation;
            String folderName = remote.getName(i).toString();
            waitUntil(() -> current.hasFolder(folderName));
            navigation = navigation.navigate(folderName);
        }
        return navigation;
    }

    @When("^I change the remote folder to '(.+)'$")
    public void iChangeTheRemoteFolderToFolder(String remotePath) throws Throwable {
        page.getSync(syncName)
            .edit()
            .enterRemotePath(remotePath)
            .save();
    }

    @And("^I delete the local '(.+)'$")
    public void iDeleteTheLocalTestfile(String filename) throws Throwable {
        Path filePath = toTmpPath(filename);
        waitUntil(() -> Files.exists(filePath));
        Files.delete(filePath);
    }

    @Then("^no '(.+)' exists in remote '(.+)'$")
    public void noTestfileExistsInRemoteFolder(String filename, String remotePath) throws Throwable {
        final BoxNavigation nav = navigate(BoxFileSystem.get("/", remotePath));
        waitUntil(() -> !nav.hasFile(filename));
    }
}
