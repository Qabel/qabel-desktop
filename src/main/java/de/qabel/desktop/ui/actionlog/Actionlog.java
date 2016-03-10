package de.qabel.desktop.ui.actionlog;

import com.sun.javafx.application.PlatformImpl;
import de.qabel.desktop.repository.DropMessageRepository;
import javafx.application.Platform;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class Actionlog {
	private final Set<PersistenceDropMessage> unseenMessages = Collections.synchronizedSet(new HashSet<>());
	private final Set<Consumer<PersistenceDropMessage>> observers = new HashSet<>();
	private final Set<Function<PersistenceDropMessage, Boolean>> filters = new HashSet<>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	public Actionlog(DropMessageRepository dropMessageRepository) {
		dropMessageRepository.addObserver((o, arg) -> {
			if (!(arg instanceof PersistenceDropMessage)) {
				return;
			}
			PersistenceDropMessage message = (PersistenceDropMessage)arg;

			if (handleMessage(message)) {
				return;
			}
			executor.submit(() -> notify(message));
		});
	}

	protected boolean handleMessage(PersistenceDropMessage message) {
		synchronized (filters) {
			for (Function<PersistenceDropMessage, Boolean> filter : filters) {
				if (!filter.apply(message)) {
					return true;
				}
			}
		}

		if (!message.isSeen()) {
			unseenMessages.add(message);
			DropMessageObserver observer = new DropMessageObserver();
			message.addObserver(observer);
			observer.setConsumer(msg -> {
				if (msg.isSeen()) {
					unseenMessages.remove(msg);
					executor.submit(() -> message.deleteObserver(observer));
					executor.submit(() -> notify(msg));
				}
			});
		}
		return false;
	}

	/**
	 * Adds a filter. When a new message is received,
	 * all filters have to return true or the message is ignored.
	 */
	public void addFilter(Function<PersistenceDropMessage, Boolean> filter) {
		synchronized (filters) {
			filters.add(filter);
		}
	}

	public void addObserver(Consumer<PersistenceDropMessage> observer) {
		synchronized (observers) {
			observers.add(observer);
		}
	}

	private void notify(PersistenceDropMessage message) {
		synchronized (observers) {
			observers.stream().forEach(observer -> observer.accept(message));
		}
	}

	public int getUnseenMessageCount() {
		return unseenMessages.size();
	}
}
