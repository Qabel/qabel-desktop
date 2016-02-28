package de.qabel.desktop.nio.boxfs;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.LinkedList;
import java.util.Set;

public class BoxFileSystem extends FileSystem {
	@Override
	public FileSystemProvider provider() {
		return new BoxFileSystemProvider();
	}

	@Override
	public void close() throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public boolean isOpen() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		LinkedList<Path> roots = new LinkedList<>();
		roots.add(new BoxPath(this, getSeparator()));
		return roots;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		throw new NotImplementedException();
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return null;
	}

	public static Path get(String first, String... more) {
		return new BoxFileSystem().getPath(first, more);
	}

	public static Path get(Path oldPath) {
		return new BoxFileSystem().getPath(oldPath);
	}

	public Path getPath(Path oldPath) {
		Path path = new BoxPath(this, looksAbsolute(oldPath) ? getSeparator() : "");
		for (int i = 0; i < oldPath.getNameCount(); i++) {
			path = path.resolve(oldPath.getName(i));
		}
		return path;
	}

	private boolean looksAbsolute(Path oldPath) {
		return oldPath.isAbsolute() || oldPath.startsWith(oldPath.getFileSystem().getSeparator());
	}

	@Override
	public Path getPath(String first, String... more) {
		Path path = new BoxPath(this, first);
		for (String part : more) {
			path = path.resolve(part);
		}
		return path;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		throw new NotImplementedException();
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new NotImplementedException();
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new NotImplementedException();
	}
}
