package de.qabel.desktop.ui;

import javafx.scene.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class LayoutControllerTest extends AbstractControllerTest {

	public static final int MIN_ITEM_COUNT = 2;

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
