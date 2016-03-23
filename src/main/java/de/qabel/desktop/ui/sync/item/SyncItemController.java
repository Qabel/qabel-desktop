package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.management.HasProgress;
import de.qabel.desktop.daemon.sync.BoxSync;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.transfer.FxProgressModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncItemController extends AbstractController implements Initializable {
	@Inject
	private ClientConfiguration clientConfiguration;

	@Inject
	private BoxSyncConfig syncConfig;

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

	Alert confirmationDialog;
	private ResourceBundle resources;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;
		FXBoxSyncConfig fxConfig = new FXBoxSyncConfig(syncConfig);

		name.textProperty().bind(fxConfig.nameProperty());
		localPath.textProperty().bind(fxConfig.localPathProperty());
		remotePath.textProperty().bind(fxConfig.remotePathProperty());

		Syncer syncer = syncConfig.getSyncer();
		if (syncer instanceof BoxSync) {
			boxSync = (BoxSync) syncer;
		}
		if (syncConfig.getSyncer() instanceof HasProgress && syncConfig.getSyncer() instanceof BoxSync) {
			progress.progressProperty().bind(new FxProgressModel((HasProgress) syncConfig.getSyncer()).progressProperty());
			progress.progressProperty().addListener((observable, oldValue, newValue) -> {
				updateSyncStatus();
			});
		}
		updateSyncStatus();
	}

	public void delete() {
		tryOrAlert(() -> {
			String contentText = getString(resources, "deleteSyncConfirmation", syncConfig.getName());
			confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.YES, ButtonType.CANCEL);
			confirmationDialog.setTitle(resources.getString("deleteSyncConfirmationHeadline"));
			confirmationDialog.setHeaderText(null);
			confirmationDialog.showAndWait()
					.ifPresent(buttonType -> {
						try {
							if (buttonType != ButtonType.YES) {
								return;
							}
							if (syncConfig.getSyncer() != null) {
								try {
									syncConfig.getSyncer().stop();
								} catch (InterruptedException e) {
									// best effort
									alert("error while stopping sync: " + e.getMessage(), e);
								} finally {
									syncConfig.setSyncer(null);
									clientConfiguration.getBoxSyncConfigs().remove(syncConfig);
								}
							}
						} finally {
							confirmationDialog = null;
						}
					});
		});
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
