package de.qabel.desktop.daemon.sync.worker.index;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SyncState {
    private boolean existing;
    private Long mtime;
    private Long size;

    /**
     * Creates default sync state (not synced at all = not existing, without information)
     */
    public SyncState() {
        this(false, null, null);
    }

    public SyncState(boolean existing, Long mtime, Long size) {
        this.existing = existing;
        this.mtime = mtime;
        this.size = size;
    }

    public boolean isExisting() {
        return existing;
    }

    public Long getMtime() {
        return mtime;
    }

    public Long getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SyncState)) {
            return false;
        }

        SyncState otherState = (SyncState) obj;
        if (otherState.getMtime() == null && !nullOrZero(getMtime())) {
            return false;
        }
        if (otherState.getSize() == null && !nullOrZero(getSize())) {
            return false;
        }

        return otherState.isExisting() == existing
            && (nullOrZero(otherState.getMtime()) == nullOrZero(mtime)
                || otherState.getMtime().equals(mtime))
            && (nullOrZero(otherState.getSize()) == nullOrZero(getSize())
                || otherState.getSize().equals(size));
    }

    private static boolean nullOrZero(Long value) {
        return value == null || value == 0L;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
            .append(existing)
            .append(mtime)
            .append(size)
            .toHashCode();
    }
}
