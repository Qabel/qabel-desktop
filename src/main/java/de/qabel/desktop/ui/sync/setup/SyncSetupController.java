package de.qabel.desktop.ui.sync.setup;

import de.qabel.core.config.Account;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.remotefs.dialog.RemoteFSDirectoryChooser;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SyncSetupController extends AbstractController implements Initializable {
    @FXML
    TextField name;

    @FXML
    TextField localPath;

    @FXML
    TextField remotePath;

    @FXML
    TextField identity;

    @FXML
    protected Button start;

    @FXML
    Button chooseLocalPath;

    @FXML
    Button chooseRemotePath;

    @Inject
    BoxVolumeFactory boxVolumeFactory;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private BoxSyncRepository boxSyncRepository;

    private StringProperty nameProperty;
    private BooleanProperty validProperty = new SimpleBooleanProperty();
    private StringProperty localPathProperty;
    private StringProperty remotePathProperty;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameProperty = name.textProperty();
        localPathProperty = localPath.textProperty();
        remotePathProperty = remotePath.textProperty();


        BooleanProperty nameValid = new SimpleBooleanProperty();
        BooleanProperty localPathValid = new SimpleBooleanProperty();
        BooleanProperty remotePathValid = new SimpleBooleanProperty();

        nameValid.bind(getNameValidityCondition());
        localPathValid.bind(getLocalPathValidityCondition());
        remotePathValid.bind(getRemotePathValidityCondition());

        validProperty.bind(
                nameValid
                .and(localPathValid)
                .and(remotePathValid)
        );

        start.disableProperty().bind(validProperty.not());

        nameValid.addListener(createErrorStyleAttacher(name));
        localPathValid.addListener(createErrorStyleAttacher(localPath));
        remotePathValid.addListener(createErrorStyleAttacher(remotePath));
        updateErrorState(name, nameValid.get());
        updateErrorState(localPath, localPathValid.get());
        updateErrorState(remotePath, remotePathValid.get());

        fixIdentity();

        chooseLocalPath.onActionProperty().setValue(event -> {
            File localPath = new DirectoryChooser().showDialog(null);
            if (localPath == null) {
                return;
            }
            localPathProperty.setValue(localPath.getAbsolutePath());
        });

        chooseRemotePath.onActionProperty().setValue(event -> {
            try {
                RemoteFSDirectoryChooser directoryChooser = new RemoteFSDirectoryChooser(
                        resources,
                        boxVolumeFactory.getVolume(
                                clientConfiguration.getAccount(),
                                clientConfiguration.getSelectedIdentity()
                        )
                );
                directoryChooser.getDialogPane().getStylesheets().addAll(stage.getScene().getRoot().getStylesheets());
                directoryChooser.showAndWait()
                .filter(path -> path != null)
                .ifPresent(path1 -> remotePathProperty.setValue(path1.toString()));
            } catch (QblStorageException e) {
                alert("failed to open RemoteFS directory chooser", e);
            }
        });
    }

    private BooleanBinding getRemotePathValidityCondition() {
        return remotePathProperty.isNotEmpty();
    }

    private BooleanBinding getLocalPathValidityCondition() {
        return localPathProperty.isNotEmpty();
    }

    private BooleanBinding getNameValidityCondition() {
        return nameProperty.isNotEmpty();
    }

    private void fixIdentity() {
        if (clientConfiguration.getSelectedIdentity() != null) {
            identity.setText(clientConfiguration.getSelectedIdentity().getAlias());
        }
    }

    private ChangeListener<Boolean> createErrorStyleAttacher(Node element) {
        return (observable, oldValue, isValid) -> {
            if (isValid.equals(oldValue)) {
                return;
            }

            updateErrorState(element, isValid);
        };
    }

    private void updateErrorState(Node element, Boolean newValue) {
        if (newValue) {
            element.getStyleClass().remove("error");
        } else {
            element.getStyleClass().add("error");
        }
    }

    public void setName(String name) {
        nameProperty.set(name);
    }

    public boolean isValid() {
        return validProperty.get();
    }

    public void setLocalPath(String localPath) {
        localPathProperty.set(localPath);
    }

    public void setRemotePath(String remotePath) {
        remotePathProperty.set(remotePath);
    }

    public void createSyncConfig() {
        tryOrAlert(() -> {
            Account account = clientConfiguration.getAccount();
            Path lPath = Paths.get(localPathProperty.get());
            Path rPath = BoxFileSystem.get(remotePathProperty.get());
            BoxSyncConfig config = new DefaultBoxSyncConfig(nameProperty.get(), lPath, rPath, clientConfiguration.getSelectedIdentity(), account);
            boxSyncRepository.save(config);
            close();
        });
    }

    public void close() {
        if (stage != null) {
            Platform.runLater(stage::close);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
