package de.qabel.desktop.daemon.management;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;
import static org.junit.Assert.assertEquals;

public class AbstractTransactionTest {
	private final List<Object> updates = new LinkedList<>();

	@Test
	public void notifiesOnSuccessfulClose() {
		try (Transaction t = new DummyTransaction()) {
			t.toState(FINISHED);
			t.onSuccess(() -> updates.add(FINISHED));
		}
		assertEquals("missing success notification", 1, updates.size());
	}

	@Test
	public void notifiesOnFailedClose() {
		try (Transaction t = new DummyTransaction()) {
			t.toState(FAILED);
			t.onFailure(() -> updates.add(FAILED));
		}
		assertEquals("missing failure notification", 1, updates.size());
	}

	@Test
	public void notifiesOnSkippedClose() {
		try (Transaction t = new DummyTransaction()) {
			t.toState(SKIPPED);
			t.onSkipped(() -> updates.add(SKIPPED));
		}
		assertEquals("missing skipped notification", 1, updates.size());
	}
}