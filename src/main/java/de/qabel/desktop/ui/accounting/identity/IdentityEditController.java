package de.qabel.desktop.ui.accounting.identity;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXToggleButton;
import de.qabel.core.config.Identity;
import de.qabel.core.index.IndexService;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;

public class IdentityEditController extends AbstractController implements Initializable {
    @FXML
    JFXSpinner uploadProgress;

    @FXML
    Pane identityEdit;

    @FXML
    TextField alias;

    @FXML
    TextField email;

    @FXML
    TextField phone;

    @FXML
    Button saveIdentity;

    @FXML
    JFXToggleButton privateToggle;

    @FXML
    Label privateLabel;

    @FXML
    Label privateHint;

    @Inject
    Identity identity;

    @Inject
    IdentityRepository identityRepository;

    @Inject
    private IndexService indexService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFromIdentity();

        saveIdentity.textProperty().bind(
            new When(privateToggle.selectedProperty())
                .then(resources.getString("saveIdentity"))
                .otherwise(resources.getString("saveAndUploadIdentity"))
        );

        saveIdentity.visibleProperty().bind(uploadProgress.visibleProperty().not());
        saveIdentity.managedProperty().bind(saveIdentity.visibleProperty());
        uploadProgress.managedProperty().bind(uploadProgress.visibleProperty());
    }

    private void initFromIdentity() {
        setAlias(identity.getAlias());
        setEmail(identity.getEmail());
        setPhone(identity.getPhone());
        setPrivate(!identity.isUploadEnabled());
    }

    private static final int MIN_SPIN_TIME = 500;

    @FXML
    void saveIdentity() {
        long start = currentTimeMillis();

        updateIdentityFromView();
        showUploadProgress();
        CheckedRunnable timer = () -> sleep(max(0, MIN_SPIN_TIME - (currentTimeMillis() - start)));

        new Thread(() -> saveAndUpload(timer)).start();
    }

    private void saveAndUpload(CheckedRunnable syntheticSleep) {
        try {
            identityRepository.save(identity);
            if (identity.isUploadEnabled()) {
                indexService.updateIdentity(identity, null);
            } else {
                indexService.removeIdentity(identity);
            }
            try {
                syntheticSleep.run();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            Platform.runLater(finishHandler);
        } catch (PersistenceException e) {
            alert("Cannot save Identity!", e);
        } finally {
            Platform.runLater(this::hideUploadProgress);
        }
    }

    private void hideUploadProgress() {
        uploadProgress.setVisible(false);
    }

    private void showUploadProgress() {
        uploadProgress.setVisible(true);
    }

    private void updateIdentityFromView() {
        identity.setAlias(getAlias());
        identity.setEmail(getEmail());
        identity.setPhone(getPhone());
        identity.setUploadEnabled(!isPrivate());
    }

    void setAlias(String alias) {
        this.alias.setText(alias);
    }

    void setEmail(String email) {
        this.email.setText(email);
    }

    void setPhone(String phone) {
        this.phone.setText(phone);
    }

    void setPrivate(boolean isPrivate) {
        privateToggle.setSelected(isPrivate);
    }

    String getAlias() {
        return alias.getText();
    }

    String getEmail() {
        return email.getText();
    }

    String getPhone() {
        return phone.getText();
    }

    boolean isPrivate() {
        return privateToggle.isSelected();
    }

    void clearFields() {
        setAlias("");
        setEmail("");
        setPhone("");
        setPrivate(false);
    }

    private Runnable finishHandler = () -> {};

    public void onFinish(Runnable finishHandler) {
        this.finishHandler = finishHandler;
    }
}
