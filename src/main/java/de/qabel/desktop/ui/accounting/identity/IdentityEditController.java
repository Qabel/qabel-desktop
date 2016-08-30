package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityEditController extends AbstractController implements Initializable {

    @FXML
    AnchorPane identityEdit;

    @FXML
    TextField aliasField;

    @FXML
    TextField emailField;

    @FXML
    TextField phoneField;

    ResourceBundle resourceBundle;

    @Inject
    protected Identity identity;

    @Inject
    protected IdentityRepository identityRepository;

    @Inject
    private Pane layoutWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        prefillDataFromIdentity();
    }

    private void prefillDataFromIdentity() {
        setAliasField(identity.getAlias());
        setEmailField(identity.getEmail());
        setPhoneField(identity.getPhone());
    }

    @FXML
    void updateIdentity() {
        identity.setAlias(getAliasField());
        identity.setEmail(getEmailField());
        identity.setPhone(getPhoneField());
        saveIdentity(identity);
    }

    void saveIdentity(Identity identity) {
        tryOrAlert(() -> identityRepository.save(identity));
    }

    public void show() {
        addToLayout();
        identityEdit.setVisible(true);
    }

    public void hide() {
        identityEdit.setVisible(false);
        removeFromLayout();
    }

    private void addToLayout() {
        if (!layoutWindow.getChildren().contains(identityEdit)) {
            layoutWindow.getChildren().add(identityEdit);
        }
    }

    private void removeFromLayout() {
        if (layoutWindow.getChildren().contains(identityEdit)) {
            layoutWindow.getChildren().remove(identityEdit);
        }
    }


    public boolean isShowing() {
        return identityEdit.isVisible();
    }

    void setAliasField(String aliasField) {
        this.aliasField.setText(aliasField);
    }

    void setEmailField(String emailField) {
        this.emailField.setText(emailField);
    }

    void setPhoneField(String phoneField) {
        this.phoneField.setText(phoneField);
    }

    String getAliasField() {
        return aliasField.getText();
    }

    String getEmailField() {
        return emailField.getText();
    }

    String getPhoneField() {
        return phoneField.getText();
    }

}
