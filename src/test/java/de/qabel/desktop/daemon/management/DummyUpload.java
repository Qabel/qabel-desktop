package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class DummyUpload implements Upload {
	@Override
	public long transactionAge() {
		return 0;
	}

	@Override
	public TYPE getType() {
		return null;
	}

	@Override
	public BoxVolume getBoxVolume() {
		return null;
	}

	@Override
	public Path getSource() {
		return null;
	}

	@Override
	public Path getDestination() {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean isDir() {
		return false;
	}

	@Override
	public Long getMtime() {
		return null;
	}

	@Override
	public Transaction onSuccess(Runnable runnable) {
		return this;
	}

	@Override
	public Transaction onFailure(Runnable runnable) {
		return this;
	}

	@Override
	public Transaction onSkipped(Runnable runnable) {
		return this;
	}

	@Override
	public long getStagingDelayMillis() {
		return 0;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public boolean hasSize() {
		return false;
	}

	@Override
	public long getProgress() {
		return 0;
	}

	@Override
	public void setProgress(long progress) {

	}

	@Override
	public void setSize(long size) {

	}

	@Override
	public STATE getState() {
		return null;
	}

	@Override
	public void toState(STATE state) {

	}

	@Override
	public void close() {

	}
}
