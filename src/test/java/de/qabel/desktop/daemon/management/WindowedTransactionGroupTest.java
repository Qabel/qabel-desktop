package de.qabel.desktop.daemon.management;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;
import static org.junit.Assert.assertEquals;

public class WindowedTransactionGroupTest extends TransactionGroupTest {

	private UploadStub t1 = new UploadStub();
	private UploadStub t2 = new UploadStub();
	private final WindowedTransactionGroup group = new WindowedTransactionGroup();

	@Test
	public void testClosesWindowWhenAllTransactionsAreFinished() {
		t1.setSize(100);
		t1.setTransferred(100);

		WindowedTransactionGroup group = new WindowedTransactionGroup();
		group.add(t1);
		t1.toState(FINISHED);

		UploadStub t2 = new UploadStub();
		t2.setSize(100);
		t2.setTransferred(0);
		group.add(t2);

		assertEquals(0.0, group.getProgress(), 0.001);
	}

	@Test
	public void givenUnfinishedProgress_allTransactionsRemainInWindow() {
		t1.setSize(100);
		t1.setTransferred(0);

		t2.setSize(100);
		t2.setTransferred(0);

		group.add(t1);
		group.add(t2);

		t1.setTransferred(100);
		t1.toState(FINISHED);

		assertEquals(0.5, group.getProgress(), 0.001);
	}

	@Test
	public void notifiesOnProgressChangeViaAdd() {
		List<Object> updates = new LinkedList<>();
		TransactionStub upload1 = new TransactionStub();
		WindowedTransactionGroup group = new WindowedTransactionGroup();
		upload1.setSize(100);
		group.addObserver((o, arg) -> updates.add(arg));

		group.add(upload1);
		assertEquals(1, updates.size());
	}

	@Test
	public void doesntAddskippedTransactions() {
		t1.setSize(100);
		t1.setTransferred(0);
		t1.toState(SKIPPED);

		group.add(t1);
		assertEquals(1.0, group.getProgress(), 0.001);
	}
}
