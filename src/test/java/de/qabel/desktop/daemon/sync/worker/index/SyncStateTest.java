package de.qabel.desktop.daemon.sync.worker.index;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SyncStateTest {
    @Test
    public void createsMissingStateByDefault() {
        SyncState state = new SyncState();
        assertThat(state.isExisting(), is(false));
        assertThat(state.getMtime(), is(nullValue()));
        assertThat(state.getSize(), is(nullValue()));
    }

    @Test
    public void providesValues() {
        SyncState state = new SyncState(true, 10L, 20L);
        assertThat(state.isExisting(), is(true));
        assertThat(state.getMtime(), is(10L));
        assertThat(state.getSize(), is(20L));
    }

    @Test
    public void equality() {
        assertThat(new SyncState(), equalTo(new SyncState()));
        assertThat(new SyncState().hashCode(), is(equalTo(new SyncState().hashCode())));
    }

    @Test
    public void unequality() {
        assertUnequal(new SyncState(false, 0L, 0L), new SyncState(true, 0L, 0L));
        assertUnequal(new SyncState(false, 0L, 0L), new SyncState(false, 10L, 0L));
        assertUnequal(new SyncState(false, 0L, 0L), new SyncState(false, 0L, 10L));
    }

    private void assertUnequal(SyncState actual, SyncState expected) {
        assertThat(actual, is(not(equalTo(expected))));
        assertThat(actual.hashCode(), is(not(equalTo(expected.hashCode()))));
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void nullsafeEquals() {
        SyncState state1 = new SyncState(true, null, null);
        SyncState state2 = new SyncState(true, 0L, 0L);

        assertThat(state1.equals(state1), is(true));
        assertThat(state1.equals(state2), is(true));
    }

    @Test
    public void nullsafeUnequals() {
        SyncState state1 = new SyncState(true, null, 10L);
        SyncState state2 = new SyncState(true, 10L, null);
        SyncState state3 = new SyncState(true, 10L, 10L);

        assertThat(state1.equals(state2), is(false));
        assertThat(state2.equals(state1), is(false));
        assertThat(state3.equals(state2), is(false));
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    public void doesntCompareWithNull() {
        assertThat(new SyncState().equals(null), is(false));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void doesntCompareWithOtherClasses() {
        assertThat(new SyncState().equals("test"), is(false));
    }
}
