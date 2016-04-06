package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import org.junit.Test;

import static org.junit.Assert.*;

public class OfflineControllerGuiTest extends AbstractGuiTest<OfflineController> {

    @Override
    protected FXMLView getView() {
        return new OfflineView();
    }

    @Test
    public void testVisibility() throws Exception {
        assertFalse(controller.offlineIndicator.isVisible());

        networkStatus.offline();

        waitUntil(() -> controller.offlineIndicator.isVisible());

        networkStatus.online();

        waitUntil(() -> !controller.offlineIndicator.isVisible());
    }
}
