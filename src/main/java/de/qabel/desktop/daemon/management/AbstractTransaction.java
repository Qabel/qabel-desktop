package de.qabel.desktop.daemon.management;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;

public abstract class AbstractTransaction implements Transaction {
	private STATE state = STATE.INITIALIZING;
	protected Long mtime;
	private List<Runnable> successHandler = new LinkedList<>();
	private List<Runnable> failureHandler = new LinkedList<>();
	private List<Runnable> skippedHandler = new LinkedList<>();
	private long creationTime = System.currentTimeMillis();
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
	public long transactionAge() {
		return System.currentTimeMillis() - creationTime;
	}
}
