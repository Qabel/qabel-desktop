package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Entity;
import de.qabel.core.config.Persistable;
import de.qabel.core.drop.DropMessage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;


public class PersistenceDropMessage extends Persistable {
	DropMessage dropMessage;
	Entity receiver;
	Entity sender;
	Boolean sent;
	Boolean seen = true;

	private transient List<Consumer<PersistenceDropMessage>> observers;

	public PersistenceDropMessage(DropMessage dropMessage, Entity from, Entity to, Boolean sent, Boolean seen) {
		this.dropMessage = dropMessage;
		this.receiver = to;
		this.sent = sent;
		this.sender = from;
		this.seen = seen;
	}

	public Entity getSender() {
		return sender;
	}

	public void setSender(Entity sender) {
		this.sender = sender;
		notifyObservers();
	}

	public DropMessage getDropMessage() {
		return dropMessage;
	}

	public void setDropMessage(DropMessage dropMessage) {
		this.dropMessage = dropMessage;
		notifyObservers();
	}

	public Entity getReceiver() {
		return receiver;
	}

	public void setReceiver(Entity receiver) {
		this.receiver = receiver;
		notifyObservers();
	}

	public Boolean isSent() {
		return sent;
	}

	public Boolean isSeen() {
		return seen;
	}

	public void setSeen(Boolean seen) {
		this.seen = seen;
		notifyObservers();
	}

	private synchronized List<Consumer<PersistenceDropMessage>> getObservers() {
		if (observers == null) {
			observers = Collections.synchronizedList(new LinkedList<>());
		}
		return observers;
	}

	public synchronized void addObserver(Consumer<PersistenceDropMessage> observer) {
		getObservers().add(observer);
	}

	public synchronized void deleteObserver(Consumer<PersistenceDropMessage> observer) {
		getObservers().remove(observer);
	}

	public synchronized void notifyObservers() {
		getObservers().stream().forEach(consumer -> consumer.accept(this));
	}
}
