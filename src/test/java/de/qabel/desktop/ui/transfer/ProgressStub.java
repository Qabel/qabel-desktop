package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgress;

import java.util.Observable;

public class ProgressStub extends Observable implements HasProgress<ProgressStub> {
	public double progress;
	public long totalSize;
	public long currentSize;

	@Override
	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
		setChanged();
		notifyObservers();
	}

	@Override
	public ProgressStub onProgress(Runnable runnable) {
		addObserver((o, arg) -> runnable.run());
		return this;
	}

	@Override
	public long totalSize() {
		return totalSize;
	}

	@Override
	public long currentSize() {
		return currentSize;
	}
}
