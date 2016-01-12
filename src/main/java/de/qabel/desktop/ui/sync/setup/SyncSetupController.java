package de.qabel.desktop.ui.sync.setup;

import de.qabel.desktop.ui.AbstractController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SyncSetupController extends AbstractController implements Initializable {
	@FXML
	TextField name;

	@FXML
	TextField localPath;

	@FXML
	TextField remotePath;

	private StringProperty nameProperty = new SimpleStringProperty();
	private BooleanProperty validProperty = new SimpleBooleanProperty(true);
	private StringProperty localPathProperty = new SimpleStringProperty();
	private StringProperty remotePathProperty = new SimpleStringProperty();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		BooleanProperty nameValid = new SimpleBooleanProperty(true);
		nameValid.bind(nameProperty.isNotEmpty());
		BooleanProperty localPathValid = new SimpleBooleanProperty(true);
		localPathValid.bind(localPathProperty.isNotEmpty());
		BooleanProperty remotePathValid = new SimpleBooleanProperty(true);
		remotePathValid.bind(remotePathProperty.isNotEmpty());

		validProperty.bind(
				nameValid
				.and(localPathValid)
				.and(remotePathValid)
		);

		nameValid.addListener(createErrorStyleAttacher(name));
		localPathValid.addListener(createErrorStyleAttacher(localPath));
		remotePathValid.addListener(createErrorStyleAttacher(remotePath));
	}

	private ChangeListener<Boolean> createErrorStyleAttacher(Node element) {
		return (observable, oldValue, newValue) -> {
			if (newValue.equals(oldValue)) {
				return;
			}

			if (newValue) {
				element.getStyleClass().remove("error");
			} else {
				element.getStyleClass().add("error");
			}
		};
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
}
