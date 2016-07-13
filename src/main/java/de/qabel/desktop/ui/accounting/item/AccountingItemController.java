package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.menuqr.MenuQRController;
import de.qabel.desktop.ui.accounting.menuqr.MenuQRView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AccountingItemController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @FXML
    Label alias;
    @FXML
    Label mail;

    @FXML
    Pane avatarContainer;

    @FXML
    RadioButton selectedRadio;

    @FXML
    private Button edit;

    @Inject
    private Identity identity;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private Pane layoutWindow;

    TextInputDialog dialog;

    private MenuQRView menuqrView;
    private MenuQRController menuqrController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        alias.textProperty().addListener((o, a, b) -> updateAvatar());
        alias.setText(identity.getAlias());

        if (clientConfiguration.hasAccount()) {
            Account account = clientConfiguration.getAccount();
            mail.setText(account.getUser());
        }

        updateSelection();
        initializeMenu();
        clientConfiguration.onSelectIdentity(i -> updateSelection());
    }

    private void updateAvatar() {
        new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
    }

    private void updateSelection() {
        selectedRadio.setSelected(identity.equals(clientConfiguration.getSelectedIdentity()));
    }

    public Identity getIdentity() {
        return identity;
    }

    public void edit(ActionEvent actionEvent) {
        dialog = new TextInputDialog(identity.getAlias());
        dialog.setHeaderText(null);
        dialog.setTitle(resourceBundle.getString("accountingItemChangeAlias"));
        dialog.setContentText(resourceBundle.getString("accountingItemNewAlias"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setAlias);
    }

    private void initializeMenu() {
        menuqrView = new MenuQRView();
        menuqrView.getView(layoutWindow.getChildren()::add);
        menuqrController = (MenuQRController) menuqrView.getPresenter();
    }

    public void openMenuQR(MouseEvent event) {
        menuqrController.showMenu(getIdentity(), event.getSceneX() + layoutWindow.getScene().getWindow().getX(), event.getSceneY() + layoutWindow.getScene().getWindow().getY() + edit.getHeight());
    }

    protected void setAlias(String alias) {
        identity.setAlias(alias);
        try {
            identityRepository.save(identity);
            this.alias.setText(alias);
        } catch (PersistenceException e) {
            alert("Failed to save identity", e);
        }
    }

    public void selectIdentity(ActionEvent actionEvent) {
        if (selectedRadio.isSelected()) {
            clientConfiguration.selectIdentity(identity);
        }
    }
}
