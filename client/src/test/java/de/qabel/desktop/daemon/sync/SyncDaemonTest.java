package de.qabel.desktop.daemon.sync;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.FakeSyncer;
import de.qabel.desktop.daemon.sync.worker.FakeSyncerFactory;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.inmemory.InMemoryBoxSyncRepository;
import de.qabel.desktop.ui.sync.DummyBoxSyncConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SyncDaemonTest extends AbstractSyncTest {
    private SyncDaemon daemon;
    private BoxSyncRepository boxSyncConfigRepo = new InMemoryBoxSyncRepository();
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
        waitForSyncerStart();

        assertTrue(daemon.getSyncers().get(0) instanceof FakeSyncer);
        assertSame(config, ((FakeSyncer)daemon.getSyncers().get(0)).config);
    }

    @Test(timeout = 1000L)
    public void loadsNewConfigs() {
        startDaemon();

        boxSyncConfigs.add(config);
        waitForSyncerStart();

        FakeSyncer fakeSyncer = (FakeSyncer) daemon.getSyncers().get(0);
        assertSame(config, fakeSyncer.config);
        waitUntil(() -> fakeSyncer.started);
    }

    @Test(timeout = 10000L)
    public void restartsWithChangedConfig() {
        startDaemon();
        boxSyncConfigs.add(config);
        waitForSyncerStart();

        FakeSyncer firstSyncer = (FakeSyncer) config.getSyncer();
        daemon.restart(config);
        waitUntil(() -> firstSyncer != config.getSyncer(), 10000L);
        FakeSyncer secondSyncer = (FakeSyncer) config.getSyncer();
        assertSame(config, secondSyncer.config);
        waitUntil(() -> secondSyncer.started);
        assertTrue(firstSyncer.stopped);
        assertEquals(1, daemon.getSyncers().size());
        assertSame(secondSyncer, daemon.getSyncers().get(0));
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

    protected void waitForSyncerStart() {
        waitUntil(() -> daemon.getSyncers().size() == 1);
    }
}
