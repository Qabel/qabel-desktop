package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
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

    @Inject
    private Pane layoutWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        update();
    }

    private void prefillDataFromIdentity() {
        setAlias(identity.getAlias());
        setEmail(identity.getEmail());
        setPhone(identity.getPhone());
    }


    @FXML
    void saveIdentity() {
        identity.setAlias(getAlias());
        identity.setEmail(getEmail());
        identity.setPhone(getPhone());
        try {
            identityRepository.save(identity);
        } catch (PersistenceException e) {
            alert("Cannot save Identity!", e);
        }
    }

    private void update() {
        prefillDataFromIdentity();
    }

    public void show() {
        identityEdit.setVisible(true);
    }

    public void hide() {
        identityEdit.setVisible(false);
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

}
