package de.qabel.desktop.ui;

import de.qabel.core.accounting.BoxClientStub;
import de.qabel.core.exceptions.QblInvalidCredentials;
import javafx.scene.Node;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
public class LayoutControllerTest extends AbstractControllerTest {

    public static final int MIN_ITEM_COUNT = 2;

    @Test
    public void getQuotaStateFailuresThenHidesQuotaBars() {
        LayoutController controller = createController();
        ((BoxClientStub) controller.boxClient).ioException = new IOException("crashed IO");
        try {
            controller.boxClient.getQuotaState();
        } catch (IOException | QblInvalidCredentials e) {
            assertFalse(controller.quotaBlock.isVisible());
            assertFalse(controller.quotaDescription.isVisible());
        }

    }

    @Test
    public void testFillQuotaInformation() throws IOException, QblInvalidCredentials {
        Locale.setDefault(new Locale("te", "ST"));
        LayoutController controller = createController();
        assertEquals("30%", controller.quota.getText());
        assertEquals(30, (int) controller.quotaBar.getMinWidth());
        assertEquals("667.57 MB free / 953.67 MB", controller.quotaDescription.getText());
    }

    @Test
    public void testFillQuotaInformationLocalizedToGerman() throws IOException, QblInvalidCredentials {
        Locale.setDefault(new Locale("de", "DE"));
        LayoutController controller = createController();
        assertThat(controller.quotaDescription.getText(), containsString("667,57"));
    }

    @Test
    public void testHidesNaviItemsByDefault() throws Exception {
        clientConfiguration.selectIdentity(null);
        LayoutController controller = createController();
        assertEquals("navi items were not hidden without identity", MIN_ITEM_COUNT, countManagedNaviItems(controller));
    }

    private LayoutController createController() {
        LayoutView view = new LayoutView();
        return (LayoutController) view.getPresenter();
    }

    @Test
    public void testShowsNaviItemsWithIdentityFromStart() throws Exception {
        LayoutController controller = createController();
        assertTrue("navi items are hidden but there is an identity", countManagedNaviItems(controller) > MIN_ITEM_COUNT);
    }

    @Test
    public void testShowsItemsWhenIdentityIsSelected() {
        clientConfiguration.selectIdentity(null);

        LayoutController controller = createController();
        clientConfiguration.selectIdentity(identityBuilderFactory.factory().withAlias("bob").build());
        waitUntil(() -> countManagedNaviItems(controller) > MIN_ITEM_COUNT, () -> "nav items are not shown when identity is selected");
    }

    private int countManagedNaviItems(LayoutController controller) {
        int managed = 0;
        for (Node item : controller.navi.getChildren()) {
            if (item.isManaged()) {
                managed++;
            }
        }
        return managed;
    }
}
