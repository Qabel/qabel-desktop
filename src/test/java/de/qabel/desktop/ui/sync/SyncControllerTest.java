package de.qabel.desktop.ui.sync;

import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SyncControllerTest extends AbstractControllerTest {
    @Test
    public void loadsItems() throws Exception {
        BoxSyncConfig boxConfig = new DummyBoxSyncConfig();
        boxSyncRepository.save(boxConfig);

        SyncController controller = createController();

        assertNotNull(controller.syncItemNodes);
        AsyncUtils.assertAsync(() -> assertThat(controller.syncItemNodes, hasSize(1)));
    }

    private SyncController createController() {
        SyncView view = new SyncView();
        view.getView();
        return (SyncController)view.getPresenter();
    }
}
