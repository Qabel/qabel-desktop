package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncItemController extends AbstractController implements Initializable {
	@Inject
	private BoxSyncConfig config;

	@FXML
	private Label name;

	@FXML
	private Label localPath;

	@FXML
	private Label remotePath;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FXBoxSyncConfig fxConfig = new FXBoxSyncConfig(config);

		name.textProperty().bind(fxConfig.nameProperty());
		localPath.textProperty().bind(fxConfig.localPathProperty());
		remotePath.textProperty().bind(fxConfig.remotePathProperty());
	}
}
