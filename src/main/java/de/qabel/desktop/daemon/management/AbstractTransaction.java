package de.qabel.desktop.daemon.management;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;

public abstract class AbstractTransaction extends Observable implements Transaction {
	private STATE state = STATE.INITIALIZING;
	protected Long mtime;
	private List<Runnable> successHandler = new LinkedList<>();
	private List<Runnable> failureHandler = new LinkedList<>();
	private List<Runnable> skippedHandler = new LinkedList<>();
	private List<Runnable> progressHandler = new LinkedList<>();
	private long creationTime = System.currentTimeMillis();
	private Long size;
	private long progress = 0L;

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
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public boolean hasSize() {
		return size != null;
	}

	@Override
	public long getProgress() {
		return progress;
	}

	@Override
	public void setProgress(long progress) {
		this.progress = progress;
		progressHandler.forEach(Runnable::run);
	}
}
