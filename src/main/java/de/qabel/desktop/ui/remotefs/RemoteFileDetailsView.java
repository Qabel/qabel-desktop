package de.qabel.desktop.ui.remotefs;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxObject;

public class RemoteFileDetailsView extends QabelFXMLView {
	public RemoteFileDetailsView(BoxNavigation navigation, BoxObject object) {
		super(singleObjectMap("navigation", navigation, "boxObject", object));
	}
}
