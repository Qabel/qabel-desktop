package de.qabel.desktop.daemon.sync;

import de.qabel.desktop.config.*;
import de.qabel.desktop.daemon.sync.worker.FakeSyncer;
import de.qabel.desktop.daemon.sync.worker.FakeSyncerFactory;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.inmemory.InMemoryBoxSyncRepisitory;
import de.qabel.desktop.ui.sync.DummyBoxSyncConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SyncDaemonTest extends AbstractSyncTest {
    private SyncDaemon daemon;
    private BoxSyncRepository boxSyncConfigRepo = new InMemoryBoxSyncRepisitory();
    private Thread thread;
    private BoxSyncConfig config;
    private ObservableList<BoxSyncConfig> boxSyncConfigs;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        boxSyncConfigs = FXCollections.observableList(boxSyncConfigRepo.findAll());
        boxSyncConfigRepo.onAdd(boxSyncConfigs::add);
        boxSyncConfigRepo.onDelete(boxSyncConfigs::remove);
        daemon = new SyncDaemon(boxSyncConfigs, new FakeSyncerFactory());
        thread = new Thread(daemon);
        config = new DummyBoxSyncConfig();
    }

    @Test(timeout = 1000L)
    public void loadsExistingConfigs() {
        boxSyncConfigs.add(config);

        startDaemon();
        waitForConfig();

        assertTrue(daemon.getSyncers().get(0) instanceof FakeSyncer);
        assertSame(config, ((FakeSyncer)daemon.getSyncers().get(0)).config);
    }

    @Test(timeout = 1000L)
    public void loadsNewConfigs() {
        startDaemon();

        boxSyncConfigs.add(config);
        waitForConfig();

        FakeSyncer fakeSyncer = (FakeSyncer) daemon.getSyncers().get(0);
        assertSame(config, fakeSyncer.config);
        waitUntil(() -> fakeSyncer.started);
    }

    @Override
    @After
    public void tearDown() throws InterruptedException {
        daemon.stop();
        if (thread.isAlive()) {
            thread.interrupt();
        }
        thread.join(1000);
        super.tearDown();
    }

    protected void startDaemon() {
        thread.start();
        waitUntil(() -> daemon.started);
    }

    protected void waitForConfig() {
        waitUntil(() -> daemon.getSyncers().size() == 1);
    }
}
