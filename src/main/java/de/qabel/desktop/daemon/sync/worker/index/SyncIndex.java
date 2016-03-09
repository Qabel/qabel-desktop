package de.qabel.desktop.daemon.sync.worker.index;


import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class SyncIndex extends Observable implements Serializable {
	private static final long serialVersionUID = 4345641723570920407L;
	private Map<String, SyncIndexEntry> index = new HashMap<>();

	public void update(Path localPath, Long localMtime, boolean exists) {
		String key = localPath.toString();
		index.put(key, new SyncIndexEntry(localPath, localMtime, exists));
		setChanged();
		notifyObservers(index.get(key));
	}

	public boolean isUpToDate(Path localPath, Long localMtime, boolean existing) {
		String key = localPath.toString();
		boolean known = index.containsKey(key);
		if (!known) {
			return false;
		}
		SyncIndexEntry entry = index.get(key);
		Long knownMtime = entry.getLocalMtime();
		return entry.isExisting() == existing && mtimeMatches(localMtime, knownMtime);
	}

	protected long mtimeDiff(Long localMtime, Long knownMtime) {
		if (localMtime == null || knownMtime == null)
			return -1;

		return localMtime - knownMtime;
	}

	protected boolean mtimeMatches(Long localMtime, Long knownMtime) {
		return localMtime == null && knownMtime == null
				|| knownMtime != null && localMtime != null && knownMtime.equals(localMtime);
	}

	public boolean hasAlreadyBeenDeleted(Path localPath, Long mtime) {
		String key = localPath.toString();
		if (!index.containsKey(key)) {
			return false;
		}
		SyncIndexEntry entry = index.get(key);
		return !entry.isExisting() && entry.getLocalMtime() > mtime;
	}

	public SyncIndexEntry get(Path localPath) {
		return index.get(localPath.toString());
	}

	public void clear() {
		index.clear();
		setChanged();
		notifyObservers();
	}
}
