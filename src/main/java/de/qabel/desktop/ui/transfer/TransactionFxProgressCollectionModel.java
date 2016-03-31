package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import de.qabel.desktop.daemon.management.Transaction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TransactionFxProgressCollectionModel extends FxProgressCollectionModel<Transaction> {
	private ObjectProperty<Transaction> currentTransaction = new SimpleObjectProperty<>();
	private BooleanProperty running = new SimpleBooleanProperty(false);

	public TransactionFxProgressCollectionModel(HasProgressCollection<?, Transaction> progress) {
		super(progress);
		onChange(this::update);
	}

	private void update(Transaction transaction) {
		if (transaction == null || transaction.getState() != Transaction.STATE.RUNNING) {
			boolean running = totalItemsProperty().isNotEqualTo(currentItemsProperty()).get();
            currentTransaction.setValue(running ? transaction : null);
			this.running.setValue(running);
			return;
		}
		running.setValue(true);
		currentTransaction.setValue(transaction);
	}

	public BooleanProperty runningProperty() {
		return running;
	}

	public ObjectProperty<Transaction> currentTransactionProperty() {
		return currentTransaction;
	}
}
