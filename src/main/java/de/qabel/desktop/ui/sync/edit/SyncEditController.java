package de.qabel.desktop.ui.sync.edit;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.ui.sync.setup.SyncSetupController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SyncEditController extends SyncSetupController {
    @FXML
    TextField name;

    @FXML
    TextField localPath;

    @FXML
    TextField remotePath;

    @FXML
    TextField identity;

    @FXML
    Button save;

    @FXML
    Button chooseLocalPath;

    @FXML
    Button chooseRemotePath;

    @Inject
    BoxSyncConfig syncConfig;

    @Inject
    BoxSyncRepository boxSyncRepository;

    @Inject
    SyncDaemon syncDaemon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        start = save;
        super.initialize(location, resources);

        localPath.setText(syncConfig.getLocalPath().toString());
        remotePath.setText(syncConfig.getRemotePath().toString());
        name.setText(syncConfig.getName());
    }

    @FXML
    public void save() {
        tryOrAlert(() -> {
            syncConfig.setName(name.getText());
            syncConfig.setLocalPath(Paths.get(localPath.getText()).toAbsolutePath());
            syncConfig.setRemotePath(BoxFileSystem.get(remotePath.getText()));
            boxSyncRepository.save(syncConfig);
            syncDaemon.restart(syncConfig);

            close();
        });
    }
}
