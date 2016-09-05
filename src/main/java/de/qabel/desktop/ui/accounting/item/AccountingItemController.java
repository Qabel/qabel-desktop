package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuView;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private Button menu;

    @Inject
    private Identity identity;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private Pane layoutWindow;

    private IdentityContextMenuView identityMenuView;
    private IdentityContextMenuController identityMenuController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        alias.textProperty().addListener((o, a, b) -> updateAvatar());
        alias.setText(identity.getAlias());

        if (clientConfiguration.hasAccount()) {
            Account account = clientConfiguration.getAccount();
            mail.setText(account.getUser());
        }

        identity.attach(() -> Platform.runLater(() -> {
            alias.setText(identity.getAlias());
        }));

        updateSelection();
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

    private void initializeMenu() {
        final Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("identity", identity);
        identityMenuView = new IdentityContextMenuView(injectionContext::get);
        identityMenuView.getView(layoutWindow.getChildren()::add);
        identityMenuController = (IdentityContextMenuController) identityMenuView.getPresenter();

    }

    public void openMenuQR(MouseEvent event) {
        initializeMenu();
        Platform.runLater(() -> identityMenuController.openMenu(event.getSceneX()
            + layoutWindow.getScene().getWindow().getX(), event.getSceneY()
            + layoutWindow.getScene().getWindow().getY() + menu.getHeight()));
    }

    public void selectIdentity() {
        if (selectedRadio.isSelected()) {
            clientConfiguration.selectIdentity(identity);
        }
    }
}
