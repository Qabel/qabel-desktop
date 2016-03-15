package de.qabel.desktop.nio.boxfs;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoxFileSystemTest {
	private BoxFileSystem fs = new BoxFileSystem();

	@Test
	public void testPathResolution() {
		Path path = fs.getPath("first", "second", "third");
		assertEquals("first/second/third", path.toString());
	}

	@Test
	public void testRootResolution() {
		assertEquals("/first", fs.getPath("/", "first").toString());
	}

	@Test
	public void testPathConversion() {
		Path result = fs.getPath(Paths.get("/first"));
		assertTrue(result instanceof BoxPath);
		assertEquals("/first", result.toString());
	}
}
