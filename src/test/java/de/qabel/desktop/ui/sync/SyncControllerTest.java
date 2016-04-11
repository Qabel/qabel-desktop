package de.qabel.desktop.ui.sync;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SyncControllerTest extends AbstractControllerTest {
    @Test
    public void loadsItems() throws Exception {
        BoxSyncConfig boxConfig = new DummyBoxSyncConfig();
        boxSyncConfigRepository.save(boxConfig);

        SyncController controller = createController();

        assertNotNull(controller.syncItemNodes);
        assertEquals(1, controller.syncItemNodes.size());
    }

    private SyncController createController() {
        SyncView view = new SyncView();
        view.getView();
        return (SyncController)view.getPresenter();
    }
}
