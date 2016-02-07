package de.qabel.desktop.daemon.management;

public interface HasProgress<T> {
	double getProgress();
	T onProgress(Runnable runnable);
}
