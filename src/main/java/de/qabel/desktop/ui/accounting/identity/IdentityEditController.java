package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityEditController extends AbstractController implements Initializable {

    @FXML
    AnchorPane identityEdit;

    @FXML
    TextField alias;

    @FXML
    TextField email;

    @FXML
    TextField phone;

    private ResourceBundle resourceBundle;

    @Inject
    Identity identity;

    @Inject
    protected IdentityRepository identityRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        initFromIdentity();
    }

    private void initFromIdentity() {
        setAlias(identity.getAlias());
        setEmail(identity.getEmail());
        setPhone(identity.getPhone());
    }

    @FXML
    void saveIdentity() {
        updateIdentityFromView();
        try {
            identityRepository.save(identity);
        } catch (PersistenceException e) {
            alert("Cannot save Identity!", e);
        }
    }

    private void updateIdentityFromView() {
        identity.setAlias(getAlias());
        identity.setEmail(getEmail());
        identity.setPhone(getPhone());
    }

    public void show() {
        identityEdit.setVisible(true);
    }

    void hide() {
        identityEdit.setVisible(false);
    }

    public boolean isShowing() {
        return identityEdit.isVisible();
    }

    public void setAlias(String alias) {
        this.alias.setText(alias);
    }

    public void setEmail(String email) {
        this.email.setText(email);
    }

    void setPhone(String phone) {
        this.phone.setText(phone);
    }

    public String getAlias() {
        return alias.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    String getPhone() {
        return phone.getText();
    }

    void clearFields() {
        setAlias("");
        setEmail("");
        setPhone("");
    }
}
