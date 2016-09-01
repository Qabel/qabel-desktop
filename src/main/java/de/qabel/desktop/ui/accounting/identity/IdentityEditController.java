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

    ResourceBundle resourceBundle;

    @Inject
    Identity identity;

    @Inject
    protected IdentityRepository identityRepository;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        prefillDataFromIdentity();
    }

    private void prefillDataFromIdentity() {
        setAlias(identity.getAlias());
        setEmail(identity.getEmail());
        setPhone(identity.getPhone());
    }


    @FXML
    void saveIdentity() {
        update();
        try {
            identityRepository.save(identity);
        } catch (PersistenceException e) {
            alert("Cannot save Identity!", e);
        }
    }

    void update() {
        if (identity.getAlias().equals(getAlias())) {
            identity.setAlias(getAlias());
        }
        if (identity.getEmail().equals(getEmail())) {
            identity.setEmail(getEmail());
        }
        if (identity.getPhone().equals(getPhone())) {
            identity.setPhone(getPhone());
        }
    }


    public void show() {
        identityEdit.setVisible(true);
    }

    public void hide() {
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

    public void setPhone(String phone) {
        this.phone.setText(phone);
    }

    public String getAlias() {
        return alias.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    public String getPhone() {
        return phone.getText();
    }

    public void clearFields() {
        setAlias("");
        setEmail("");
        setPhone("");
    }
}
