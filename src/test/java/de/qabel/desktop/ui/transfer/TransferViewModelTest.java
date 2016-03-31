package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.TransactionGroup;
import de.qabel.desktop.daemon.management.TransactionStub;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static de.qabel.desktop.daemon.management.Transaction.STATE.RUNNING;
import static org.junit.Assert.*;

public class TransferViewModelTest {
	private TransferViewModel model;
	private TransactionGroup transactions;

	@Before
	public void setUp() {
		transactions = new TransactionGroup();
		model = new TransferViewModel(transactions);
		model.usePlaformThread = false;
	}

	@Test
	public void usefulDefaultProperties() {
		assertFalse(isTransactionImageVisible());
		assertEquals("", getPercentLabel());
		assertEquals("", getTransactionLabel());
	}

	@Test
	public void updatesProgress() {
		transactions.add(createRunningTransaction(100L, 50L));
		assertEquals(50, getPercent());
		assertEquals("50 %", getPercentLabel());
		assertTrue(isTransactionImageVisible());
	}

	private boolean isTransactionImageVisible() {
		return model.currentTransactionImageVisible().get();
	}

	private String getPercentLabel() {
		return model.currentTransactionPercentLabel().get();
	}

	private long getPercent() {
		return model.currentTransactionPercent().get();
	}

	private String getTransactionLabel() {
		return model.currentTransactionLabel().get();
	}

	@Test
	public void usesTransactionLabelRenderer() {
		model.setTransactionLabelRenderer(t -> "custom label");
		transactions.add(createRunningTransaction());
		assertEquals("custom label", getTransactionLabel());
	}

	private TransactionStub createRunningTransaction() {
		return createRunningTransaction(100L, 0L);
	}

	private TransactionStub createRunningTransaction(Long size, Long transferred) {
		return createTransaction(size, transferred, RUNNING);
	}

	private TransactionStub createTransaction(Long size, Long transferred, Transaction.STATE state) {
		TransactionStub transaction = new TransactionStub();
		transaction.toState(state);
		transaction.setSize(size);
		transaction.setTransferred(transferred);

		// more data to satisfy renderers
		transaction.destination = Paths.get("exampleFile");

		return transaction;
	}
}
