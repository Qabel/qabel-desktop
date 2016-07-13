package de.qabel.desktop.daemon.sync.worker.index;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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

        assertThat(state1, equalTo(state1));
        assertThat(state1, equalTo(state2));
    }

    @Test
    public void nullsafeUnequals() {
        SyncState state1 = new SyncState(true, null, 10L);
        SyncState state2 = new SyncState(true, 10L, null);
        SyncState state3 = new SyncState(true, 10L, 10L);

        assertThat(state1, not(equalTo(state2)));
        assertThat(state2, not(equalTo(state1)));
        assertThat(state3, not(equalTo(state2)));
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    public void doesntCompareWithNull() {
        assertThat(new SyncState(), not(equalTo(null)));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void doesntCompareWithOtherClasses() {
        assertThat(new SyncState(), not(equalTo("test")));
    }
}
