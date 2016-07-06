package de.qabel.desktop.ui.accounting.identityContextMenu;

import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.item.AccountingItemController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private Pane layoutWindow;

    @FXML
    private AnchorPane menuQR;

    @FXML
    private VBox vboxMenu;

    @FXML
    Pane avatarContainer;

    @FXML
    Label test;

    @Inject
    private IdentityRepository identityRepository;

    TextInputDialog dialog;

    private QRCodeView qrcodeView;
    private QRCodeController qrcodeController;
    private AccountingItemView accountingItemView;
    private AccountingItemController accountingItemController;
    private PopOver popOver;
    private Label alias;
    private RadioButton selectedRadio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;


        clientConfiguration.onSelectIdentity(i -> updateSelection());
    }

    private void initializeQRPopup() {
        if (qrcodeView == null) {
            qrcodeView = new QRCodeView();
            qrcodeView.getView(layoutWindow.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void setAlias(Label alias) {
        this.alias = alias;
    }

    private void updateSelection() {
        selectedRadio.setSelected(identity.equals(clientConfiguration.getSelectedIdentity()));
    }

    public void setSelectedRadio(RadioButton radiobutton){
        selectedRadio = radiobutton;
        updateSelection();
    }

    public void setAvatarContainer(Pane pane) {
        this.avatarContainer = pane;
        addListener();
    }

    private void addListener() {
        alias.textProperty().addListener((o, a, b) -> updateAvatar());
        alias.setText(identity.getAlias());
    }


    public void showMenu(double coordPopOverX, double coordPopOverY) {
        initializePopOver();
        popOver.show(menuQR, coordPopOverX, coordPopOverY);
    }

    private void updateAvatar() {
        new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
    }

    private void initializePopOver() {
        popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setContentNode(new VBox(vboxMenu));
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
    }

    private void closeMenu() {
        popOver.hide();
        layoutWindow.getChildren().remove(this);
    }

    public void openQRCode() {
        closeMenu();
        initializeQRPopup();
        qrcodeController.setIdentity(identity);
        qrcodeController.showPopup();

    }

    public void editIdentity(MouseEvent event) {
        dialog = new TextInputDialog(identity.getAlias());
        dialog.setX(layoutWindow.getScene().getWindow().getX() + (event.getSceneX() * 2));
        dialog.setY(layoutWindow.getScene().getWindow().getY() + (event.getSceneY() * 4));
        dialog.setHeaderText(null);
        dialog.setTitle(resourceBundle.getString("accountingItemChangeAlias"));
        dialog.setContentText(resourceBundle.getString("accountingItemNewAlias"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setAlias);
    }


    protected void setAlias(String alias) {
        identity.setAlias(alias);
        try {
            identityRepository.save(identity);
            this.alias.setText(alias);
            updateSelection();
        } catch (PersistenceException e) {
            alert("Failed to save identity", e);
        }
    }
}
