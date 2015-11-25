package de.qabel.desktop.storage;

import java.util.Arrays;

public class BoxFile extends BoxObject {
	public String block;
	public Long size;
	public Long mtime;
	public byte[] key;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BoxFile boxFile = (BoxFile) o;

		if (block != null ? !block.equals(boxFile.block) : boxFile.block != null) return false;
		if (name != null ? !name.equals(boxFile.name) : boxFile.name != null) return false;
		if (size != null ? !size.equals(boxFile.size) : boxFile.size != null) return false;
		if (mtime != null ? !mtime.equals(boxFile.mtime) : boxFile.mtime != null) return false;
		return Arrays.equals(key, boxFile.key);

	}

	@Override
	public int hashCode() {
		int result = block != null ? block.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (size != null ? size.hashCode() : 0);
		result = 31 * result + (mtime != null ? mtime.hashCode() : 0);
		result = 31 * result + (key != null ? Arrays.hashCode(key) : 0);
		return result;
	}

	public BoxFile(String block, String name, Long size, Long mtime, byte[] key) {
		super(name);
		this.block = block;
		this.size = size;
		this.mtime = mtime;
		this.key = key;
	}
}
