package de.qabel.desktop.nio.boxfs;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class BoxPathTest {
	private BoxFileSystem fs = new BoxFileSystem();

	@Test
	public void emptyPathIsNotAbsolute() {
		assertFalse(path("").isAbsolute());
	}

	private BoxPath path(String path) {
		return new BoxPath(fs, path);
	}

	@Test
	public void relativePathIsNotAbsolute() {
		assertFalse(path("../../file").isAbsolute());
	}

	@Test
	public void rootIsAbsolute() {
		assertTrue(path("/").isAbsolute());
	}

	@Test
	public void absolutePathIsAbsolute() {
		assertTrue(path("/absolute/path").isAbsolute());
	}

	@Test
	public void relativePathHasNoRoot() {
		assertNull(path("relative").getRoot());
	}

	@Test
	public void emptyPathHasNoFilename() {
		assertNull(path("").getFileName());
	}

	@Test
	public void singleElementFilePathIsItsPath() {
		assertEquals(path("file").toString(), path("file").getFileName().toString());
	}

	@Test
	public void rootHasNoFilename() {
		assertNull(path("/").getFileName());
	}

	@Test
	public void multiElementPathKnowsFilename() {
		assertEquals("file", path("/directory/file").getFileName().toString());
	}

	@Test
	public void multiElementDirectoryNameIsADirectoriesFilename() {
		assertEquals("subdirectory", path("/directory/subdirectory/").getFileName().toString());
	}

	@Test
	public void absolutePathHasNameCount() {
		assertEquals(3, path("/first/second/third").getNameCount());
	}

	@Test
	public void relativePathHasNameCount() {
		assertEquals(2, path("first/second").getNameCount());
	}

	@Test
	public void rootElementHasNoNames() {
		assertEquals(0, path("/").getNameCount());
	}

	@Test
	public void namesGetExtracted() {
		BoxPath path = path("/directory/subdirectory/");
		assertEquals("directory", path.getName(0).toString());
		assertEquals("subdirectory", path.getName(1).toString());
	}

	@Test
	public void subpath() {
		assertEquals("sub/path", path("/absolute/sub/path/example").subpath(1,2).toString());
	}

	@Test
	public void resolvesRelativePaths() {
		assertEquals("/old/new", path("/old").resolve("new").toString());
	}

	@Test
	public void resolvesAbsolutePaths() {
		assertEquals("/new", path("/old").resolve("/new").toString());
	}

	@Test
	public void absolutePathDoesNotStartWithRelativePath() {
		assertFalse(path("/folder").startsWith("file"));
	}

	@Test
	public void absolutePathStartsWithRoot() {
		assertTrue(path("/folder").startsWith(path("/")));
	}

	@Test
	public void relativePathDoesNotStartWithAbsolutePath() {
		assertFalse(path("relative").startsWith(path("/")));
	}

	@Test
	public void prefixDoesNotStartWithLongerPath() {
		assertFalse(path("/prefix").startsWith(path("/prefix/suffix")));
	}

	@Test
	public void contentOfNamesIsCompared() {
		assertFalse(path("/one/two").startsWith(path("/alpha/beta")));
	}

	@Test
	public void equalStartIsTheStart() {
		assertTrue(path("/one/two").startsWith(path("/one")));
	}

	@Test
	public void pathStartWithItself() {
		assertTrue(path("/one/two").startsWith(path("/one/two")));
	}

	@Test
	public void startWithWorksWithStrings() {
		assertTrue(path("/one/two").startsWith("/one"));
	}

	@Test
	public void relativePathDoesNotEndWithAbsolutePath() {
		assertFalse(path("path").endsWith("/path"));
	}

	@Test
	public void relativePathEndsWithItseld() {
		assertTrue(path("path").endsWith("path"));
	}

	@Test
	public void nothingEndsWithAnAbsolutePath() {
		assertFalse(path("/path").endsWith("/path"));
	}

	@Test
	public void stringEndsWithSuffix() {
		assertTrue(path("/prefix/suffix").endsWith("suffix"));
	}

	@Test
	public void doesNotEndWithNonSuffix() {
		assertFalse(path("/some/thing/suffix").endsWith("thing"));
	}

	@Test
	public void relativizeSimpleSuffix() {
		Path prefix = path("/prefix/dir");
		Path fullPath = path("/prefix/dir/suffix/dir");
		assertEquals(fullPath.toString(), prefix.resolve(prefix.relativize(fullPath)).toString());
	}

	@Test
	public void relativizeOwnPathResultsInEmptyPath() {
		Path path = path("/some/path");
		assertEquals("", path.relativize(path).toString());
		assertEquals(path.toString(), path.resolve(path.relativize(path)).toString());
	}

	@Test
	public void relativePathResolution() {
		Path source = path("/a/b");
		Path destination = path("/a/c");
		assertEquals("../c", source.relativize(destination).toString());
	}

	@Test
	public void absolutePathIsItsAbsolutePath() {
		assertEquals(path("/absolute").toAbsolutePath().toString(), path("/absolute").toString());
	}

	@Test
	public void absolutePathIsRelativePathPrefixedWithRoot() {
		assertEquals("/relative", path("relative").toAbsolutePath().toString());
	}

	@Test
	public void relativePathsHaveAParent() {
		assertEquals("relative", path("relative/path").getParent().toString());
	}

	@Test
	public void absolutePathsHaveAParent() {
		assertEquals("/absolute", path("/absolute/path").getParent().toString());
	}

	@Test
	public void relativeSingleElementPathHasNoParent() {
		assertNull(path("element").getParent());
	}

	@Test
	public void rootHasNoParent() {
		assertNull(path("/").getParent());
	}

	@Test
	public void childOfRootHasRootAsParent() {
		assertEquals("/", path("/absolute").getParent().toString());
	}
}
