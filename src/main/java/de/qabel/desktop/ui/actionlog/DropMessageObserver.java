package de.qabel.desktop.ui.actionlog;

import java.util.function.Consumer;

public class DropMessageObserver implements Consumer<PersistenceDropMessage> {
	private Consumer<PersistenceDropMessage> consumer;

	@Override
	public void accept(PersistenceDropMessage persistenceDropMessage) {
		if (consumer != null) {
			consumer.accept(persistenceDropMessage);
		}
	}

	public void setConsumer(Consumer<PersistenceDropMessage> consumer) {
		this.consumer = consumer;
	}
}
