package de.qabel.desktop.daemon.management;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultLoadManagerTest {
	@Test
	public void queuesUploads() {
		Upload upload = new FakeUpload();
		LoadManager manager = new DefaultLoadManager();
		manager.addUpload(upload);
		assertEquals(1, manager.getUploads().size());
		assertSame(upload, manager.getUploads().get(0));
	}
}