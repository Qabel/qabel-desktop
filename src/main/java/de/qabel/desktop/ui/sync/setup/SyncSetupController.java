package de.qabel.desktop.ui.sync.setup;

import de.qabel.core.config.Account;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.inject.Inject;
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

	@Inject
	private ClientConfiguration clientConfiguration;

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
		nameValid.bind(getNameValidityCondition());
		BooleanProperty localPathValid = new SimpleBooleanProperty();
		localPathValid.bind(getLocalPathValidityCondition());
		BooleanProperty remotePathValid = new SimpleBooleanProperty();
		remotePathValid.bind(getRemotePathValidityCondition());

		validProperty.bind(
				nameValid
				.and(localPathValid)
				.and(remotePathValid)
		);

		nameValid.addListener(createErrorStyleAttacher(name));
		localPathValid.addListener(createErrorStyleAttacher(localPath));
		remotePathValid.addListener(createErrorStyleAttacher(remotePath));
		updateErrorState(name, nameValid.get());
		updateErrorState(localPath, localPathValid.get());
		updateErrorState(remotePath, remotePathValid.get());

		fixIdentity();
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
		Account account = clientConfiguration.getAccount();
		Path lPath = Paths.get(localPathProperty.get());
		Path rPath = Paths.get(remotePathProperty.get());
		BoxSyncConfig config = new DefaultBoxSyncConfig(nameProperty.get(), lPath, rPath, clientConfiguration.getSelectedIdentity(), account);
		clientConfiguration.getBoxSyncConfigs().add(config);
		close();
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
