package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityEditController extends AbstractController implements Initializable {


    ResourceBundle resourceBundle;

    @Inject
    protected Identity identity;

    @Inject
    protected IdentityRepository identityRepository;

    @Inject
    Pane layoutWindow;

    public BorderPane identityEdit;

    @FXML
    TextField alias;
    @FXML
    TextField email;
    @FXML
    TextField phone;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        setAlias(identity.getAlias());
        setEmail(identity.getEmail());
        setPhone(identity.getPhone());
        hide();
    }
    @FXML
    void updateIdentity() {
        identity.setAlias(getAlias());
        identity.setEmail(getEmail());
        identity.setPhone(getPhone());

        saveIdentity(identity);
    }

    void saveIdentity(Identity identity) {
        tryOrAlert(() -> identityRepository.save(identity));
    }

    public void show() {
        if (!layoutWindow.getChildren().contains(identityEdit)) {
            layoutWindow.getChildren().add(identityEdit);
        }
        identityEdit.setVisible(true);
    }

    public void hide() {
        identityEdit.setVisible(false);
        layoutWindow.getChildren().remove(identityEdit);
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
