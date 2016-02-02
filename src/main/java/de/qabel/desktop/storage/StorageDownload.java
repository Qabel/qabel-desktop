package de.qabel.desktop.storage;

import java.io.InputStream;

public class StorageDownload {
	private InputStream inputStream;
	private String mHash;
	private long size;

	public StorageDownload(InputStream inputStream, String mHash, long size) {
		this.inputStream = inputStream;
		this.mHash = mHash;
		this.size = size;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getMHash() {
		return mHash;
	}

	public long getSize() {
		return size;
	}
}
