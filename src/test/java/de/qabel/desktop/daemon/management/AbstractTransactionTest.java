package de.qabel.desktop.daemon.management;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;
import static org.junit.Assert.*;

public class AbstractTransactionTest {
    public static final double PRECISION = 0.001;
    private final List<Object> updates = new LinkedList<>();
    private final List<Object> progress = new LinkedList<>();
    private final Transaction t = new DummyTransaction();

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

    @Test
    public void knowsWhenItHasNoSize() {
        Transaction t = new DummyTransaction();
        t.setSize(0);
        assertFalse(t.hasSize());
    }

    @Test
    public void knowsWhenItHasASize() {
        DummyTransaction t = new DummyTransaction();
        t.setSize(1L);
        assertTrue(t.hasSize());
    }

    @Test
    public void notifiesOnProgress() {
        Transaction t = new DummyTransaction();
        t.onProgress(() -> progress.add(null));
        t.setTransferred(10L);

        assertEquals(1, progress.size());
    }

    @Test
    public void notifiesOnStateChange() {
        Transaction t = new DummyTransaction();
        t.onProgress(() -> progress.add(null));
        t.toState(FAILED);

        assertEquals(1, progress.size());
    }

    @Test
    public void hasProgressWithoutSize() {
        t.setSize(0);
        t.setTransferred(0);

        assertEquals(0.0, t.getProgress(), PRECISION);
    }

    @Test
    public void hasDecimalProgress() {
        t.setSize(100);
        t.setTransferred(50);
        assertEquals(0.5, t.getProgress(), PRECISION);
    }
}
