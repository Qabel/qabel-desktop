package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public interface Transaction<S extends Path, D extends Path> extends AutoCloseable, HasProgress<Transaction> {
    long transactionAge();

    TYPE getType();

    BoxVolume getBoxVolume();

    S getSource();

    D getDestination();

    boolean isValid();

    boolean isDir();

    Long getMtime();

    Transaction onSuccess(Runnable runnable);

    Transaction onFailure(Runnable runnable);

    Transaction onSkipped(Runnable runnable);

    long getStagingDelayMillis();

    /**
     * Get the size of the transaction.
     * Please check hasSize() before calling getSize() to ensure a size exists.
     *
     * @return size in bytes
     * @throws NullPointerException if no size is set
     */
    long getSize();

    boolean hasSize();

    /**
     * @return progress in bytes
     */
    long getTransferred();

    /**
     * @param progress in bytes
     */
    void setTransferred(long progress);

    void setSize(long size);

    boolean isDone();

    boolean hasStarted();


    enum TYPE { CREATE, UPDATE, DELETE }
    enum STATE { INITIALIZING, SCHEDULED, RUNNING, FINISHED, FAILED, WAITING, SKIPPED }

    STATE getState();

    void toState(STATE state);

    @Override
    void close();
}
