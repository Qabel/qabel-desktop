package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.daemon.sync.worker.BoxNavigationStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class RemoteFSGuiTest extends AbstractGuiTest<RemoteFSController> {

	@Override
	protected FXMLView getView() {
		CachedBoxNavigation nav1 = new BoxNavigationStub(null, Paths.get("/"));
		BoxVolumeStub volume = new BoxVolumeStub();
		volume.rootNavigation = nav1;
		boxVolumeFactory.boxVolume = volume;

		return new RemoteFSView();
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void optionsOnHover() {
		assertFalse(getFirstNode("#download_0").isVisible());
		assertFalse(getFirstNode("#upload_file_0").isVisible());
		assertFalse(getFirstNode("#upload_folder_0").isVisible());
		assertFalse(getFirstNode("#create_folder_0").isVisible());
		assertFalse(getFirstNode("#delete_0").isVisible());
		assertFalse(getFirstNode("#share_0").isVisible());

		moveTo("#download_0");

		assertTrue(getFirstNode("#download_0").isVisible());
		assertTrue(getFirstNode("#upload_file_0").isVisible());
		assertTrue(getFirstNode("#upload_folder_0").isVisible());
		assertTrue(getFirstNode("#create_folder_0").isVisible());
		assertTrue(getFirstNode("#delete_0").isVisible());
		assertTrue(getFirstNode("#share_0").isVisible());

		robot.moveTo(stage);

		assertFalse(getFirstNode("#download_0").isVisible());
		assertFalse(getFirstNode("#upload_file_0").isVisible());
		assertFalse(getFirstNode("#upload_folder_0").isVisible());
		assertFalse(getFirstNode("#create_folder_0").isVisible());
		assertFalse(getFirstNode("#delete_0").isVisible());
		assertFalse(getFirstNode("#share_0").isVisible());
	}
}