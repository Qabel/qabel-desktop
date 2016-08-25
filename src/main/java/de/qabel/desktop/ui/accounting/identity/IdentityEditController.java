package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityEditController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    protected Identity identity;

    @Inject
    protected IdentityRepository identityRepository;

    @FXML
    TextField alias;
    @FXML
    TextField email;
    @FXML
    TextField phone;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
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

    String getAlias() {
        return alias.getText();
    }

    String getEmail() {
        return email.getText();
    }

    String getPhone() {
        return phone.getText();
    }

    @FXML
    void updateIdentity() throws PersistenceException {
        prepareIdentityUpdate();
        identityRepository.save(identity);
    }

    private void prepareIdentityUpdate() {
        if (identity != null) {
            identity.setAlias(getAlias());
            identity.setEmail(getEmail());
            identity.setPhone(getPhone());
        }
    }
}
