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
        if (otherState.getMtime() == null && getMtime() != null && getMtime() != 0L) {
            return false;
        }
        if (otherState.getSize() == null && getSize() != null && getSize() != 0L) {
            return false;
        }

        return otherState.isExisting() == existing
            && (otherState.getMtime() == null && getMtime() == null
                || otherState.getMtime() == null && getMtime() == 0L
                || otherState.getMtime() == 0L && getMtime() == null
                || otherState.getMtime().equals(mtime))
            && (otherState.getSize() == null && getSize() == null
                || otherState.getSize() == null && getSize() == 0L
                || otherState.getSize() == 0L && getSize() == null
                || otherState.getSize().equals(size));
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
