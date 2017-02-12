package de.qabel.desktop.daemon.management;

import java.util.function.Consumer;

public interface HasProgressCollection<T, S> extends HasProgress<T> {
    T onProgress(Consumer<S> consumer);

    /**
     * get the total number of elements, representing the progress total
     */
    long totalElements();

    /**
     * get the number of finished element, representing the current progress compared to totalElements()
     */
    long finishedElements();

    int totalFiles();

    int currentFinishedFiles();
}
