package de.qabel.desktop.storage;

public abstract class BoxObject implements Comparable<BoxObject> {
	public String name;
	public byte[] key;

	public BoxObject(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(BoxObject another) {
		if (this instanceof BoxFile && another instanceof BoxFile) {
			return this.name.compareTo(another.name);
		}
		if (this instanceof BoxFolder && another instanceof BoxFolder) {
			return this.name.compareTo(another.name);
		}
		if (this instanceof BoxFile) {
			return -1;
		}
		return 1;
	}

	public String getName() {
		return name;
	}

	public byte[] getKey() {
		return key;
	}
}
