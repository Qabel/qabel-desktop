package de.qabel.desktop.ui.sync.edit;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.desktop.config.BoxSyncConfig;

public class SyncEditView extends QabelFXMLView {
    public SyncEditView(BoxSyncConfig syncConfig) {
        super(singleObjectMap("syncConfig", syncConfig));
    }
}
