package de.qabel.desktop.daemon.management;


import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultLoadManager implements LoadManager, Runnable {
	private final Logger logger = LoggerFactory.getLogger(DefaultLoadManager.class);
	private final LinkedBlockingQueue<Transaction> transactions = new LinkedBlockingQueue<>();

	@Override
	public List<Transaction> getTransactions() {
		return Arrays.asList(transactions.toArray(new Transaction[transactions.size()]));
	}

	@Override
	public void addDownload(Download download) {
		logger.trace("download added: " + download.getSource() + " to " + download.getDestination());
		transactions.add(download);
	}

	@Override
	public void addUpload(Upload upload) {
		logger.trace("upload added: " + upload.getSource() + " to " + upload.getDestination());
		transactions.add(upload);
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
				next();
			}
		} catch (InterruptedException e) {
			logger.trace("loadManager stopped: " + e.getMessage());
		}
	}

	void next() throws InterruptedException {
		Transaction transaction = transactions.take();
		logger.trace("handling transaction  " + transaction);
		try {
			if (transaction instanceof Upload) {
				upload((Upload) transaction);
			} else {
				download((Download) transaction);
			}
		} catch (QblStorageException e) {
			logger.error("Transaction failed: " + e.getMessage(), e);
		}
	}

	private void download(Download download) throws QblStorageException {
		logger.trace("skipped download  " + download);
	}

	void upload(Upload upload) throws QblStorageException {
		if (!upload.isValid()) {
			logger.trace("skipped invalid upload " + upload);
			return;
		}

		Path destination = upload.getDestination();
		Path source = upload.getSource();
		boolean isDir = Files.isDirectory(source);

		BoxVolume volume = upload.getBoxVolume();
		Path parent = destination;
		BoxNavigation dir;

		switch (upload.getType()) {
			case DELETE:
				parent = destination.getParent();
				dir = navigate(parent, volume);
				String fileName = destination.getFileName().toString();
				if (dir.hasFolder(fileName)) {
					dir.delete(dir.getFolder(fileName));
				} else {
					dir.delete(dir.getFile(fileName));
				}
				break;
			case UPDATE:
				parent = destination.getParent();
				dir = navigate(parent, volume);
				overwriteFile(dir, source, destination);
				break;
			default:
				if (!isDir) {
					parent = destination.getParent();
				}
				dir = createDirectory(parent, volume);
				if (!isDir) {
					uploadFile(dir, source, destination);
				}
				break;
		}
	}

	private BoxNavigation navigate(Path path, BoxVolume volume) throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		for (int i = 0; i < path.getNameCount(); i++) {
			nav = nav.navigate(path.getName(i).toString());
		}
		return nav;
	}

	private void uploadFile(BoxNavigation dir, Path source, Path destination) throws QblStorageException {
		dir.upload(destination.getFileName().toString(), source.toFile());
	}

	private void overwriteFile(BoxNavigation dir, Path source, Path destination) throws QblStorageException {
		dir.overwrite(destination.getFileName().toString(), source.toFile());
	}

	private BoxNavigation createDirectory(Path destination, BoxVolume volume) throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		for (int i = 0; i < destination.getNameCount(); i++) {
			String name = destination.getName(i).toString();
			try {
				nav = nav.navigate(name);
			} catch (IllegalArgumentException e) {
				nav = nav.navigate(nav.createFolder(name));
			}
		}
		return nav;
	}
}
