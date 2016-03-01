package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.HasProgress;
import de.qabel.desktop.daemon.sync.BoxSync;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.transfer.FxProgressModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class DummySyncItemController extends AbstractController implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}
