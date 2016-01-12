package de.qabel.desktop.ui.sync.setup;

import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SyncSetupControllerTest extends AbstractControllerTest {

	private SyncSetupController controller;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		SyncSetupView view = new SyncSetupView();
		Node node = view.getView();
		controller = (SyncSetupController) view.getPresenter();

		controller.setName("valid");
		controller.setLocalPath(Paths.get("path").toAbsolutePath().toString());
		controller.setRemotePath(Paths.get("path").toAbsolutePath().toString());
	}

	@Test
	public void testDetectsEmptyName() {
		controller.setName("");
		assertFalse("empty name did not invalidate form", controller.isValid());
		assertErrorClass(controller.name);
	}

	private void assertErrorClass(Node name) {
		assertTrue("field has no error class", name.getStyleClass().contains("error"));
	}

	@Test
	public void testValidatesOnValidProperties() {
		assertTrue("empty remotePath did not invalidate form", controller.isValid());
	}

	@Test
	public void testDetectsEmptyLocalPath() {
		controller.setLocalPath("");
		assertFalse("empty localPath did not invalidate form", controller.isValid());
		assertErrorClass(controller.localPath);
	}

	@Test
	public void testDetectsEmptyRemotePath() {
		controller.setRemotePath("");
		assertFalse("empty remotePath did not invalidate form", controller.isValid());
		assertErrorClass(controller.remotePath);
	}
}
