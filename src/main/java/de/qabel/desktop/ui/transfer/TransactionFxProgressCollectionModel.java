package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import de.qabel.desktop.daemon.management.Transaction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class TransactionFxProgressCollectionModel extends FxProgressCollectionModel<Transaction> {
	private BooleanProperty running = new SimpleBooleanProperty(false);

	public TransactionFxProgressCollectionModel(HasProgressCollection<?, Transaction> progress) {
		super(progress);
		onChange(this::update);
	}

	private void update(Transaction transaction) {
		if (transaction == null || transaction.getState() != Transaction.STATE.RUNNING) {
			running.setValue(totalItemsProperty().isNotEqualTo(currentItemsProperty()).get());
			return;
		}
		running.setValue(true);
	}

	public BooleanProperty runningProperty() {
		return running;
	}
}
