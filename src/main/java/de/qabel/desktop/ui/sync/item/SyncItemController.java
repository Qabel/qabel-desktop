package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.management.HasProgress;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.Upload;
import de.qabel.desktop.daemon.sync.BoxSync;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.transfer.FxProgressCollectionModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.*;
import java.awt.Insets;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncItemController extends AbstractController implements Initializable {
	private static final Image folderDownloadImg = new Image(SyncItemController.class.getResourceAsStream("/icon/add_folder.png"), 18, 18, true, true);
	private static final Image folderUploadImg = new Image(SyncItemController.class.getResourceAsStream("/icon/folder-upload.png"), 18, 18, true, true);
	private static final Image fileDownloadImg = new Image(SyncItemController.class.getResourceAsStream("/icon/download.png"), 18, 18, true, true);
	private static final Image fileUploadImg = new Image(SyncItemController.class.getResourceAsStream("/icon/upload.png"), 18, 18, true, true);

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

	@FXML
	private ImageView currentItemIcon;

	@FXML
	private Label syncStatusLabel;

	@FXML
	private Label itemStatusLabel;

	@FXML
	private Label currentItemLabel;

	private BoxSync boxSync;

	Alert confirmationDialog;
	private ResourceBundle resources;
	private FxProgressCollectionModel<Transaction> progressModel;

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
			progressModel = new FxProgressCollectionModel<>(syncConfig.getSyncer());
			progress.progressProperty().bind(progressModel.progressProperty());

			progressModel.progressProperty().addListener((observable, oldValue, newValue) -> {
				updateSyncStatus();
			});
			progressModel.currentItemProperty().addListener(observable -> {
				updateSyncStatus(progressModel.currentItemProperty().get());
			});
			progressModel.onChange(this::updateSyncStatus);
		}
		updateSyncStatus();
		syncStatusLabel.setText("");
	}

	public void open() {
		new Thread(() -> {
			tryOrAlert(() -> Desktop.getDesktop().open(syncConfig.getLocalPath().toFile()));
		}).start();
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

	private void updateSyncStatus(Transaction transaction) {
		if (transaction == null) {
			currentItemLabel.setText("");
			itemStatusLabel.setText("");
			currentItemIcon.setVisible(false);
		} else {
			if (transaction.isDone()) {
				currentItemLabel.setText("");
				itemStatusLabel.setText("");
				currentItemIcon.setVisible(false);
			} else {
				if (transaction.hasStarted() && !transaction.isDone()) {
					currentItemLabel.setText(renderTransaction(transaction));
					itemStatusLabel.setText(((int) (transaction.getProgress() * 100)) + " %");
					currentItemIcon.setImage(selectImage(transaction));
					currentItemIcon.setVisible(true);
				}
			}
		}

		updateSyncStatus();
	}

	private Image selectImage(Transaction transaction) {
		if (transaction.isDir()) {
			if (transaction instanceof Upload) {
				return folderUploadImg;
			} else {
				return folderDownloadImg;
			}
		} else {
			if (transaction instanceof Upload) {
				return fileUploadImg;
			} else {
				return folderUploadImg;
			}
		}
	}

	private String renderTransaction(Transaction transaction) {
		String filename = transaction.getDestination().getFileName().toString();
		String direction = transaction instanceof Upload ? "Remote" : "Local";
		String type = transaction.getType().toString();
		return filename + " (" + StringUtils.capitalize(type) + " " + (transaction.getSize() / 1024) + "kb)";
	}

	private void updateSyncStatus() {
		if (boxSync == null)
			return;

		syncImage.setImage(new Image(getClass().getResourceAsStream(getImage(boxSync.isSynced()))));
		syncStatusLabel.setText(getSyncStatusText());
	}

	private String getSyncStatusText() {
		if (boxSync.isSynced()) {
			return "Ready";
		}

		long totalTransfers = progressModel.totalItemsProperty().get();
		if (totalTransfers == 0) {
			return "Collecting files... " + syncConfig.getSyncer().getHistory().size();
		}
		return "Syncing: " + progressModel.currentItemsProperty().get() + " / " + totalTransfers;
	}

	@FXML
	public void showHistory() {
		tryOrAlert(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText(null);
			alert.setTitle("Sync History");

			StringBuilder history = new StringBuilder();
			syncConfig.getSyncer().getHistory().stream()
					.sorted((o1, o2) -> (int)(o1.transactionAge() - o2.transactionAge()))
					.forEach(transaction -> {
						if (history.length() != 0) {
							history.append("\n");
						}
						history.append(renderTransaction(transaction)).append(" ").append(transaction.getState());
					});

			TextArea textArea = new TextArea(history.toString());
			VBox.setMargin(textArea, new javafx.geometry.Insets(10, 0, 5, 0));
			textArea.setEditable(false);
			textArea.setWrapText(false);

			VBox.setVgrow(textArea, Priority.ALWAYS);

			VBox expansion = new VBox();

			expansion.getChildren().add(textArea);

			alert.getDialogPane().setContent(expansion);
			alert.setResizable(true);
			alert.showAndWait();
		});
	}

	private String getImage(boolean synced) {
		if (synced) {
			return "/ic_folder_black_synced.png";
		}
		return "/ic_folder_black_syncing.png";
	}
}
