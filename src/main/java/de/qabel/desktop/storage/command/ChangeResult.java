package de.qabel.desktop.storage.command;

import de.qabel.desktop.storage.BoxObject;
import de.qabel.desktop.storage.DirectoryMetadata;

public class ChangeResult<T extends BoxObject> {
	private DirectoryMetadata dm;
	private T boxObject;

	public ChangeResult(DirectoryMetadata dm, T boxObject) {
		this.dm = dm;
		this.boxObject = boxObject;
	}

	public DirectoryMetadata getDM() {
		return dm;
	}

	public T getBoxObject() {
		return boxObject;
	}
}
