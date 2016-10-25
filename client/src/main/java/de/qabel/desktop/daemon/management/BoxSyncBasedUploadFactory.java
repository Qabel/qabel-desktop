package de.qabel.desktop.daemon.management;

import de.qabel.box.storage.BoxVolume;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.WatchEvent;

import java.util.concurrent.TimeUnit;

public class BoxSyncBasedUploadFactory {
    private long syncDelayMills = TimeUnit.SECONDS.toMillis(2);

    public BoxSyncBasedUpload getUpload(BoxVolume volume, BoxSyncConfig config, WatchEvent event) {
        BoxSyncBasedUpload boxSyncBasedUpload = new BoxSyncBasedUpload(volume, config, event);
        boxSyncBasedUpload.setStagingDelayMills(syncDelayMills);
        return boxSyncBasedUpload;
    }

    public void setSyncDelayMills(long syncDelayMills) {
        this.syncDelayMills = syncDelayMills;
    }
}
