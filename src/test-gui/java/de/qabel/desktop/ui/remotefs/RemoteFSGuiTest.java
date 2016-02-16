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
		int rootIndex = 1;
		assertFalse(getFirstNode("#download_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#delete_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#share_" + rootIndex).isVisible());

		moveTo("#download_" + rootIndex);

		assertTrue(getFirstNode("#download_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#delete_" + rootIndex).isVisible());
		assertTrue(getFirstNode("#share_" + rootIndex).isVisible());

		robot.moveTo(stage);

		assertFalse(getFirstNode("#download_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_file_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#upload_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#create_folder_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#delete_" + rootIndex).isVisible());
		assertFalse(getFirstNode("#share_" + rootIndex).isVisible());
	}
}