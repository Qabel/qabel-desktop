package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistable;

import java.util.LinkedList;
import java.util.List;

public class PersistentClientConfiguration extends Persistable {
	private static final long serialVersionUID = 253036267706808630L;
	public String identitiyId;
	public String accountId;
	public List<PersistentBoxSyncConfig> boxSyncConfigs = new LinkedList<>();
}
