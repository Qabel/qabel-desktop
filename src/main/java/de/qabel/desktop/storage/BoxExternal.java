package de.qabel.desktop.storage;

import de.qabel.core.crypto.QblECPublicKey;

import java.util.Arrays;

public class BoxExternal {
	public String url;
	public String name;
	public QblECPublicKey owner;
	public byte[] key;

	public BoxExternal(String url, String name, QblECPublicKey owner, byte[] key) {
		this.url = url;
		this.name = name;
		this.owner = owner;
		this.key = key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BoxExternal that = (BoxExternal) o;

		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
		return Arrays.equals(key, that.key);

	}

	@Override
	public int hashCode() {
		int result = url != null ? url.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (owner != null ? owner.hashCode() : 0);
		result = 31 * result + (key != null ? Arrays.hashCode(key) : 0);
		return result;
	}
}
