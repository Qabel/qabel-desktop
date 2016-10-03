package de.qabel.desktop.ui.sync;

import com.airhacks.afterburner.views.FXMLView;
import cucumber.api.junit.Cucumber;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.sync.item.SyncItemController;
import de.qabel.desktop.ui.sync.item.SyncItemView;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RunWith(Cucumber.class)
public class SyncCucumberTest extends AbstractGuiTest<SyncItemController> {
    private BoxSyncConfig syncConfig;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Override
    protected FXMLView getView() {
        syncConfig = new DefaultBoxSyncConfig(
            "dummy",
            Paths.get("localPath"),
            BoxFileSystem.getRoot().resolve("remotePath"),
            identity,
            account,
            new InMemorySyncIndexFactory()
        );
        return new SyncItemView(s -> syncConfig);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
