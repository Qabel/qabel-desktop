package de.qabel.desktop.ui.sync.item;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.Upload;
import de.qabel.desktop.daemon.sync.BoxSync;
import de.qabel.desktop.daemon.sync.worker.Syncer;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.sync.edit.SyncEditController;
import de.qabel.desktop.ui.sync.edit.SyncEditView;
import de.qabel.desktop.ui.transfer.TransferViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncItemController extends AbstractController implements Initializable {
    @FXML
    Parent syncItemRoot;

    @Inject
    private BoxSyncRepository boxSyncRepository;

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
    private ColumnConstraints itemProgressColumn;

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

    @FXML
    private StackPane composedProgressPane;

    @FXML
    private VBox statusContentPane;

    private BoxSync boxSync;

    Alert confirmationDialog;
    private ResourceBundle resources;
    private TransferViewModel progressModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        FXBoxSyncConfig fxConfig = new FXBoxSyncConfig(syncConfig);

        name.textProperty().bind(fxConfig.nameProperty());
        syncItemRoot.getStyleClass().add(fxConfig.nameProperty().get());
        localPath.textProperty().bind(fxConfig.localPathProperty());
        remotePath.textProperty().bind(fxConfig.remotePathProperty());


        syncConfig.withSyncer(this::initModel);
    }

    public void initModel(Syncer syncer) {
        boxSync = syncer;
        progressModel = new TransferViewModel(syncer);
        progress.progressProperty().bind(progressModel.progressProperty());

        progressModel.progressProperty().addListener((observable, oldValue, newValue) -> {
            updateSyncStatus();
        });
        progressModel.currentItemProperty().addListener(observable -> {
            updateSyncStatus(progressModel.currentItemProperty().get());
        });

        itemStatusLabel.textProperty().bind(progressModel.currentTransactionPercentLabel());
        itemProgressColumn.percentWidthProperty().bind(progressModel.currentTransactionPercent());
        currentItemLabel.textProperty().bind(progressModel.currentTransactionLabel());
        currentItemIcon.visibleProperty().bind(progressModel.currentTransactionImageVisible());
        currentItemIcon.imageProperty().bind(progressModel.currentTransactionImage());
        currentItemLabel.visibleProperty().bind(progressModel.currentTransactionImageVisible());

        updateSyncStatus();
        syncStatusLabel.setText("");
    }

    public void open() {
        new Thread(() -> {
            tryOrAlert(() -> Desktop.getDesktop().open(syncConfig.getLocalPath().toFile()));
        }).start();
    }

    public BoxSyncConfig getSyncConfig() {
        return syncConfig;
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
                                    boxSyncRepository.delete(syncConfig);
                                }
                            }
                        } catch (PersistenceException e) {
                            throw new IllegalStateException("failed to delete sync: " + e.getMessage(), e);
                        } finally {
                            confirmationDialog = null;
                        }
                    });
        });
    }

    private void updateSyncStatus(Transaction transaction) {
        updateSyncStatus();
    }

    private String renderTransaction(Transaction transaction) {
        String filename = transaction.getDestination().getFileName().toString();
        String direction = transaction instanceof Upload ? "Remote" : "Local";
        String type = transaction.getType().toString();
        return filename + " (" + StringUtils.capitalize(type) + " " + transaction.getSize() / 1024 + "kb)";
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
            VBox.setMargin(textArea, new Insets(10, 0, 5, 0));
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

    Stage editStage;
    SyncEditController syncEditController;

    public void edit() {
        editStage = new Stage();
        SyncEditView view = new SyncEditView(syncConfig);
        Scene scene = new Scene(view.getView());
        editStage.setScene(scene);
        syncEditController = (SyncEditController) view.getPresenter();
        syncEditController.setStage(editStage);
        editStage.show();
    }
}
