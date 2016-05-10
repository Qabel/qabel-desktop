package de.qabel.desktop.daemon.management;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;

public abstract class AbstractTransaction<S extends Path, D extends Path> extends Observable implements Transaction<S,D> {
    public static final long METADATA_SIZE = 56320L;
    private STATE state = INITIALIZING;
    protected Long mtime;
    private List<Runnable> successHandler = new CopyOnWriteArrayList<>();
    private List<Runnable> failureHandler = new CopyOnWriteArrayList<>();
    private List<Runnable> skippedHandler = new CopyOnWriteArrayList<>();
    private List<Runnable> progressHandler = new CopyOnWriteArrayList<>();
    private long creationTime = System.currentTimeMillis();
    private Long size = METADATA_SIZE;  // metadata size ... imagine a random number here
    private long transferred;

    public AbstractTransaction(Long mtime) {
        this.mtime = mtime;
    }

    @Override
    public STATE getState() {
        return state;
    }

    @Override
    public void toState(STATE state) {
        this.state = state;
        progressHandler.forEach(Runnable::run);
    }

    @Override
    public Long getMtime() {
        return mtime;
    }

    @Override
    public void close() {
        if (transferred == 0) {
            setTransferred(getSize());
        }
        if (state == FAILED) {
            failureHandler.forEach(Runnable::run);
        } else if (state == FINISHED) {
            successHandler.forEach(Runnable::run);
        } else if (state == SKIPPED) {
            skippedHandler.forEach(Runnable::run);
        }
    }

    @Override
    public synchronized Transaction onSuccess(Runnable runnable) {
        successHandler.add(runnable);
        return this;
    }

    @Override
    public synchronized Transaction onFailure(Runnable runnable) {
        failureHandler.add(runnable);
        return this;
    }

    @Override
    public synchronized Transaction onSkipped(Runnable runnable) {
        skippedHandler.add(runnable);
        return this;
    }

    @Override
    public synchronized Transaction onProgress(Runnable runnable) {
        progressHandler.add(runnable);
        return this;
    }

    @Override
    public long totalSize() {
        return getSize();
    }

    @Override
    public long currentSize() {
        return getTransferred();
    }

    @Override
    public long transactionAge() {
        return System.currentTimeMillis() - creationTime;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
        progressHandler.forEach(Runnable::run);
    }

    @Override
    public boolean hasSize() {
        return size != null && size != 0;
    }

    @Override
    public long getTransferred() {
        return transferred;
    }

    @Override
    public void setTransferred(long transferred) {
        this.transferred = transferred;
        progressHandler.forEach(Runnable::run);
    }

    @Override
    public double getProgress() {
        return  getSize() > 0 ? (double)getTransferred() / (double)getSize() : isDone() ? 1.0 : 0.0;
    }

    @Override
    public boolean isDone() {
        return state == FINISHED || state == FAILED || state == SKIPPED;
    }

    @Override
    public boolean hasStarted() {
        return state != INITIALIZING && state != WAITING && state != SCHEDULED;
    }
}
