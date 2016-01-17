package de.qabel.desktop.daemon.management;

import java.util.List;

public interface LoadManager {
	List<Upload> getUploads();
	void addUpload(Upload upload);
}
