package de.qabel.desktop.daemon.management;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;

public abstract class AbstractTransaction extends Observable implements Transaction {
	public static final long METADATA_SIZE = 56320L;
	private STATE state = STATE.INITIALIZING;
	protected Long mtime;
	private List<Runnable> successHandler = new LinkedList<>();
	private List<Runnable> failureHandler = new LinkedList<>();
	private List<Runnable> skippedHandler = new LinkedList<>();
	private List<Runnable> progressHandler = new LinkedList<>();
	private long creationTime = System.currentTimeMillis();
	private Long size = METADATA_SIZE;	// metadata size ... imagine a random number here
	private long transferred = 0L;

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
		if (state == FAILED) {
			failureHandler.forEach(Runnable::run);
		} else if (state == FINISHED) {
			successHandler.forEach(Runnable::run);
		} else if (state == SKIPPED) {
			skippedHandler.forEach(Runnable::run);
		}
	}

	@Override
	public Transaction onSuccess(Runnable runnable) {
		successHandler.add(runnable);
		return this;
	}

	@Override
	public Transaction onFailure(Runnable runnable) {
		failureHandler.add(runnable);
		return this;
	}

	@Override
	public Transaction onSkipped(Runnable runnable) {
		skippedHandler.add(runnable);
		return this;
	}

	@Override
	public Transaction onProgress(Runnable runnable) {
		progressHandler.add(runnable);
		return this;
	}

	@Override
	public long transactionAge() {
		return System.currentTimeMillis() - creationTime;
	}

	@Override
	public long getSize() {
		return hasSize() ? size : METADATA_SIZE;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public boolean hasSize() {
		return size != null && size != 0;
	}

	public long getTransferred() {
		return transferred;
	}

	public void setTransferred(long transferred) {
		this.transferred = transferred;
		progressHandler.forEach(Runnable::run);
	}

	@Override
	public double getProgress() {
		return hasSize() ? getTransferred() / getSize() : (isDone() ? 1.0 : 0.0);
	}

	@Override
	public boolean isDone() {
		return state == FINISHED || state == FAILED || state == SKIPPED;
	}
}
