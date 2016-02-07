package de.qabel.desktop.daemon.management;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FAILED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;
import static org.junit.Assert.*;

public class TransactionGroupTest {
	public static final double PRECISION = 0.001;
	private UploadStub upload1 = new UploadStub();
	private UploadStub upload2 = new UploadStub();
	private TransactionGroup group = new TransactionGroup();

	@Before
	public void setUp() {
	}

	@Test
	public void givenNoTransactions_groupIsEmpty() {
		assertTrue(group.isEmpty());
	}

	@Test
	public void givenATransaction_groupIsNotEmpty() {
		group.add(upload1);
		assertFalse("group with transaction was empty", group.isEmpty());
	}

	@Test
	public void givenOneTransaction_groupHasOne() {
		group.add(upload1);
		assertEquals(1, group.size());
	}

	@Test
	public void givenZeroTransactions_groupHasZero() {
		assertEquals(0, group.size());
	}

	@Test
	public void givenNoTransactions_progressIs1() {
		double progress = 1.0;
		assertProgress(progress);
	}

	private void assertProgress(double progress) {
		assertEquals(progress, group.getProgress(), PRECISION);
	}

	@Test
	public void givenANotStartedTransaction_progressIs0() {
		upload1.setSize(100L);
		group.add(upload1);
		assertProgress(0.0);
	}

	@Test
	public void givenAStartedTransaction_progressDependsOnTransaction() {
		upload1.setSize(100);
		group.add(upload1);
		upload1.setTransferred(50);

		assertProgress(0.5);
	}

	@Test
	public void givenMultipleTransactions_progressDependsOnSize() {
		upload1.setSize(100);
		upload2.setSize(300);
		upload1.setTransferred(100);
		upload2.setTransferred(0);

		group.add(upload1);
		group.add(upload2);

		assertProgress(0.25);
	}

	@Test
	public void givenProgressChange_observersAreNotified() {
		List<Object> updates = new LinkedList<>();
		upload1.setSize(100);
		group.add(upload1);

		group.addObserver((o, arg) -> updates.add(arg));

		upload1.setTransferred(50);
		upload1.toState(FINISHED);
		upload1.toState(FAILED);
		upload1.toState(SKIPPED);
		assertEquals(4, updates.size());
	}

	@Test
	public void notifiesOnProgressChangeViaAdd() {
		List<Object> updates = new LinkedList<>();
		upload1.setSize(100);
		group.addObserver((o, arg) -> updates.add(arg));

		group.add(upload1);
		assertEquals(1, updates.size());
	}
}
