package de.qabel.desktop.exceptions;

public class QblStorageIOFailure extends QblStorageException {
	public QblStorageIOFailure(Throwable e) {
		super(e);
	}

	public QblStorageIOFailure(String s) {
		super(s);
	}
}
