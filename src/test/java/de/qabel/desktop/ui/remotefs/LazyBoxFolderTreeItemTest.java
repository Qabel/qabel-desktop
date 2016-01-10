package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class LazyBoxFolderTreeItemTest extends AbstractControllerTest {
	private FakeBoxNavigation navigation;
	private LazyBoxFolderTreeItem item;

	@Test(timeout = 1000)
	public void adjustsNameProperty() {
		navigation = new FakeBoxNavigation();
		item = new LazyBoxFolderTreeItem(createSomeFolder(), navigation);
		StringProperty nameProperty = item.getNameProperty();

		assertEquals("name", nameProperty.get());

		((FakeBoxNavigation)item.getNavigation()).loading = true;
		item.getChildren();

		assertEquals("name (loading)", nameProperty.get());

		((FakeBoxNavigation)item.getNavigation()).loading = false;
		load();

		assertEquals("name", nameProperty.get());
	}

	@Test(timeout = 1000)
	public void isLeafWithoutFiles() {
		navigation = new FakeBoxNavigation();
		item = new LazyBoxFolderTreeItem(createSomeFolder(), navigation);
		assertFalse(item.isLeaf());
		load();
		assertTrue(item.isLeaf());
	}

	@Test(timeout = 1000)
	public void synchronouslyReturnsEmptyList() {
		navigation = new FakeBoxNavigation();
		item = new LazyBoxFolderTreeItem(createSomeFolder(), navigation);
		navigation.folders.add(new BoxFolder("ref2", "name2", new byte[0]));

		assertEquals(0, item.getChildren().size());
	}

	@Test(timeout = 1000)
	public void loadsChildrenAsynchonously() throws InterruptedException {
		navigation = new FakeBoxNavigation();
		item = new LazyBoxFolderTreeItem(createSomeFolder(), navigation);
		navigation.folders.add(createSomeFolder());

		ObservableList<TreeItem<BoxObject>> children = load();

		assertEquals(1, children.size());
		assertFalse(item.isLeaf());
	}

	private BoxFolder createSomeFolder() {
		return new BoxFolder("ref", "name", new byte[0]);
	}

	private ObservableList<TreeItem<BoxObject>> load() {
		ObservableList<TreeItem<BoxObject>> children = item.getChildren();
		((FakeBoxNavigation)item.getNavigation()).loading = false;
		while(item.isLoading())
			Thread.yield();
		return children;
	}

	@Test(timeout = 1000)
	public void loadsFiles() throws Exception {
		navigation = new FakeBoxNavigation();
		navigation = new FakeBoxNavigation();
		item = new LazyBoxFolderTreeItem(createSomeFolder(), navigation);
		navigation.files.add(createSomeFile());

		ObservableList<TreeItem<BoxObject>> children = load();

		assertEquals(1, children.size());
	}

	private BoxFile createSomeFile() {
		return new BoxFile("ref2", "name2", 0L, 0L, new byte[0]);
	}

	private class FakeBoxNavigation implements BoxNavigation {
		public boolean loading = false;

		public List<BoxFile> files = new LinkedList<>();
		public List<BoxFolder> folders = new LinkedList<>();

		@Override
		public void commit() throws QblStorageException {

		}

		@Override
		public BoxNavigation navigate(BoxFolder target) throws QblStorageException {
			return null;
		}

		@Override
		public BoxNavigation navigate(BoxExternal target) {
			return null;
		}

		@Override
		public List<BoxFile> listFiles() throws QblStorageException {
			while(loading)
				Thread.yield();
			return files;
		}

		@Override
		public List<BoxFolder> listFolders() throws QblStorageException {
			while(loading)
				Thread.yield();
			return folders;
		}

		@Override
		public List<BoxExternal> listExternals() throws QblStorageException {
			return null;
		}

		@Override
		public BoxFile upload(String name, File file) throws QblStorageException {
			return null;
		}

		@Override
		public BoxFile overwrite(String name, File file) throws QblStorageException {
			return null;
		}

		@Override
		public InputStream download(BoxFile file) throws QblStorageException {
			return null;
		}

		@Override
		public BoxFolder createFolder(String name) throws QblStorageException {
			return null;
		}

		@Override
		public void delete(BoxFile file) throws QblStorageException {

		}

		@Override
		public void delete(BoxFolder folder) throws QblStorageException {

		}

		@Override
		public void delete(BoxExternal external) throws QblStorageException {

		}
	}
}