package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.Initializable;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncItemController extends AbstractController implements Initializable {
	@Inject
	private BoxSyncConfig config;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
