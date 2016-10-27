package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class FxProgressCollectionModel<T> {
    boolean usePlaformThread = true;
    private DoubleProperty progressProperty = new SimpleDoubleProperty(1.0);
    private LongProperty totalItemsProperty = new SimpleLongProperty(0);
    private LongProperty currentItemsProperty = new SimpleLongProperty(0);
    private ObjectProperty<T> currentItemProperty = new SimpleObjectProperty<>();
    private HasProgressCollection<?, T> progress;
    private List<Consumer<T>> changeHandlers = new LinkedList<>();

    public FxProgressCollectionModel(HasProgressCollection<?, T> progress) {
        setProgress(progress);
    }

    public DoubleProperty progressProperty() {
        return progressProperty;
    }

    public LongProperty totalItemsProperty() {
        return totalItemsProperty;
    }

    public LongProperty currentItemsProperty() {
        return currentItemsProperty;
    }

    public ObjectProperty<T> currentItemProperty() {
        return currentItemProperty;
    }

    public void setProgress(HasProgressCollection<?, T> progress) {
        this.progress = progress;
        progress.onProgress(this::updateProgress);
    }

    private void updateProgress(T currentItem) {
        run(() -> {
            progressProperty.set(progress.getProgress());
            currentItemProperty.set(currentItem);
            totalItemsProperty.setValue(progress.totalElements());
            currentItemsProperty.setValue(progress.finishedElements());
            for (Consumer<T> handler : changeHandlers) {
                handler.accept(currentItem);
            }
        });
    }

    private void run(Runnable runnable) {
        if (usePlaformThread) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * T max be null if no item is currently in progress (or between two items)
     */
    public void onChange(Consumer<T> changeHandler) {
        changeHandlers.add(changeHandler);
    }
}
