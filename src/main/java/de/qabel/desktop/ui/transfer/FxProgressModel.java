package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgress;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class FxProgressModel {
	private DoubleProperty progressProperty = new SimpleDoubleProperty(1.0);
	private HasProgress progress;
	private Long minimumUpdateDelay = 100l;

	public FxProgressModel() {

	}

	public FxProgressModel(HasProgress progress) {
		this();
		setProgress(progress);
	}

	/**
	 * Sets the minimum delay between two progress changes.
	 * This reduces the load by minimizing the UI refreshes when the progress is adjusted very fine grained.
	 * When progress is set to 1.0, it will always be updated, no matter what the delay is.
	 * delay = null means always update
	 */
	public void setMinimumUpdateDelay(Long delay) {
		this.minimumUpdateDelay = delay;
	}

	public DoubleProperty progressProperty() {
		return progressProperty;
	}

	public void setProgress(HasProgress progress) {
		this.progress = progress;
		progress.onProgress(this::updateProgress);
		updateProgress();
	}

	long lastUpdate = 0;

	private void updateProgress() {
		final double progress = this.progress.getProgress();
		long now = System.currentTimeMillis();
		if (progress != 1.0 && minimumUpdateDelay != null && now < lastUpdate + minimumUpdateDelay) {
			return;
		}
		lastUpdate = now;
		Platform.runLater(() -> progressProperty.set(progress));
	}
}
