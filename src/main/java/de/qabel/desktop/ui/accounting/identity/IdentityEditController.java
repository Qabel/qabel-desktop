package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityEditController extends AbstractController implements Initializable {

    @FXML
    Pane identityEdit;

    @FXML
    TextField alias;

    @FXML
    TextField email;

    @FXML
    TextField phone;

    @Inject
    Identity identity;

    @Inject
    protected IdentityRepository identityRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            finishHandler.run();
        } catch (PersistenceException e) {
            alert("Cannot save Identity!", e);
        }
    }

    private void updateIdentityFromView() {
        identity.setAlias(getAlias());
        identity.setEmail(getEmail());
        identity.setPhone(getPhone());
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

    void clearFields() {
        setAlias("");
        setEmail("");
        setPhone("");
    }

    private Runnable finishHandler = () -> {};

    public void onFinish(Runnable finishHandler) {
        this.finishHandler = finishHandler;
    }
}
