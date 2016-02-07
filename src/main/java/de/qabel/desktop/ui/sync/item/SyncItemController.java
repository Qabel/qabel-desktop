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

public class SyncItemController extends AbstractController implements Initializable {
	@Inject
	private BoxSyncConfig config;

	@FXML
	private Label name;

	@FXML
	private Label localPath;

	@FXML
	private Label remotePath;

	@FXML
	private ProgressBar progress;

	@FXML
	private ImageView syncImage;

	private BoxSync boxSync;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FXBoxSyncConfig fxConfig = new FXBoxSyncConfig(config);

		name.textProperty().bind(fxConfig.nameProperty());
		localPath.textProperty().bind(fxConfig.localPathProperty());
		remotePath.textProperty().bind(fxConfig.remotePathProperty());

		Syncer syncer = config.getSyncer();
		if (syncer instanceof BoxSync) {
			boxSync = (BoxSync) syncer;
		}
		if (config.getSyncer() instanceof HasProgress && config.getSyncer() instanceof BoxSync) {
			progress.progressProperty().bind(new FxProgressModel((HasProgress) config.getSyncer()).progressProperty());
			progress.progressProperty().addListener((observable, oldValue, newValue) -> {
				updateSyncStatus();
			});
		}
		updateSyncStatus();
	}

	private void updateSyncStatus() {
		if (boxSync == null)
			return;

		syncImage.setImage(new Image(getClass().getResourceAsStream(getImage(boxSync.isSynced()))));
	}

	private String getImage(boolean synced) {
		if (synced) {
			return "/ic_folder_black_synced.png";
		}
		return "/ic_folder_black_syncing.png";
	}
}
