package de.qabel.desktop.daemon.sync;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.daemon.sync.worker.SyncerFactory;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncDaemon implements Runnable {
	private SyncerFactory syncerFactory;
	private ObservableList<BoxSyncConfig> configs;
	private List<Syncer> syncers = new LinkedList<>();
	boolean started;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public SyncDaemon(ObservableList<BoxSyncConfig> configs, SyncerFactory syncerFactory) {
		this.configs = configs;
		this.syncerFactory = syncerFactory;
	}

	@Override
	public void run() {
		for (BoxSyncConfig config : configs) {
			start(config);
		}

		configs.addListener((ListChangeListener<BoxSyncConfig>) c -> {
			while(c.next()) {
				c.getAddedSubList().forEach(this::start);
			}
		});

		started = true;
	}

	public void stop() {
		threadPool.shutdownNow();
	}

	protected boolean start(BoxSyncConfig config) {
		Syncer syncer = syncerFactory.factory(config);
		threadPool.submit(syncer);
		return syncers.add(syncer);
	}

	protected List<Syncer> getSyncers() {
		return syncers;
	}
}
