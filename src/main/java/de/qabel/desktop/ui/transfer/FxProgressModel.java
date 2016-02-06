package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgress;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class FxProgressModel {
	private DoubleProperty progressProperty = new SimpleDoubleProperty(1.0);
	private HasProgress progress;

	public FxProgressModel() {

	}

	public FxProgressModel(HasProgress progress) {
		this();
		setProgress(progress);
	}

	public DoubleProperty progressProperty() {
		return progressProperty;
	}

	public void setProgress(HasProgress progress) {
		this.progress = progress;
		progress.onProgress(this::updateProgress);
		updateProgress();
	}

	private void updateProgress() {
		Platform.runLater(() -> progressProperty.set(this.progress.getProgress()));
	}
}
