package de.qabel.desktop.daemon.management;

public abstract class AbstractTransaction implements Transaction {
	private STATE state = STATE.INITIALIZING;
	private Long mtime;

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
}
