package de.qabel.desktop.storage;

import java.io.InputStream;

public class StorageDownload {
	private InputStream inputStream;
	private long mtime;
	private long size;

	public StorageDownload(InputStream inputStream, long mtime, long size) {
		this.inputStream = inputStream;
		this.mtime = mtime;
		this.size = size;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getMtime() {
		return mtime;
	}

	public long getSize() {
		return size;
	}
}
