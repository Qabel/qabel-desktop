package de.qabel.desktop.daemon.management;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DefaultLoadManager implements LoadManager {
	private final Logger logger = LoggerFactory.getLogger(DefaultLoadManager.class);
	private final LinkedList<Upload> uploads = new LinkedList<>();

	@Override
	public List<Upload> getUploads() {
		return Collections.unmodifiableList(uploads);
	}

	@Override
	public void addUpload(Upload upload) {
		logger.trace("upload added: " + upload.getSource());
		uploads.add(upload);
	}
}
