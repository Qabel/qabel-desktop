package de.qabel.desktop.ui;

import com.sun.org.apache.xpath.internal.operations.Quo;
import de.qabel.core.accounting.QuotaState;
import de.qabel.core.exceptions.QblInvalidCredentials;
import javafx.scene.Node;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LayoutControllerTest extends AbstractControllerTest {

    public static final int MIN_ITEM_COUNT = 2;

    //same values as the BoxClientStub
    private QuotaState expectedQuotaState = new QuotaState(24, 100);

    @Test
    public void testQuotaDescription() {
        String expected = "667,57 MB free / 953,67 MB";
        long availableQuota = 1000000000L;
        long usedQuota = 300000000L;
        String quotaDescription = createController().quotaDescription(usedQuota, availableQuota);

        assertEquals(expected, quotaDescription);
    }

    @Test
    public void testRatioByDiff() {
        int expectedRatio = 30;
        long availableQuota = 1000000000L;
        long usedQuota = 300000000L;
        int ratio = createController().ratioByDiff(usedQuota, availableQuota);

        assertEquals(expectedRatio, ratio);
    }

    @Test
    public void testFillQuotaInformation() throws IOException, QblInvalidCredentials {
        LayoutController controller = createController();
        QuotaState quota = controller.boxClient.getQuotaState();

        String expectedDesc = controller.quotaDescription(expectedQuotaState.getSize(), expectedQuotaState.getQuota());
        int expectedRatio = controller.ratioByDiff(expectedQuotaState.getSize(), expectedQuotaState.getQuota());

        assertEquals(expectedQuotaState.getQuota(), quota.getQuota());
        assertEquals(expectedQuotaState.getSize(), quota.getSize());
        assertEquals(expectedRatio + "%", controller.quota.getText());
        assertEquals(expectedRatio, (int) controller.provider.getMinWidth());
        assertEquals(expectedDesc, controller.quotaDescription.getText());
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
