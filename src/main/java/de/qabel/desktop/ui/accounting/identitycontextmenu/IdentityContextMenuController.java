package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditView;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import de.qabel.desktop.ui.util.Popup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    private ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    Pane layoutWindow;

    @FXML
    AnchorPane identityContextMenu;

    @FXML
    VBox contextMenu;

    private QRCodeView qrcodeView;
    public QRCodeController qrcodeController;

    IdentityEditView identityEditView;
    public IdentityEditController identityEditController;

    PopOver popOver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }

    private void createPopOver() {
        popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
        popOver.setContentNode(contextMenu);
    }

    public void openMenu() {
        createPopOver();
        popOver.show(identityContextMenu);
    }

    public void openMenu(double coordPopOverX, double coordPopOverY) {
        createPopOver();
        popOver.show(identityContextMenu, coordPopOverX, coordPopOverY);
    }

    public void closeMenu() {
        if (popOver != null && popOver.isShowing()) {
            Platform.runLater(popOver::hide);
        }
    }

    private void createQrCodePopup(Pane container) {
        if (qrcodeView == null) {
            qrcodeView = new QRCodeView(generateInjection("identity", identity));
            qrcodeView.getView(container.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void openQRCode() {
        closeMenu();
        createQrCodePopup(layoutWindow);
        qrcodeController.show();
    }

    public void openIdentityEdit() {
        closeMenu();
        createIdentityEdit(layoutWindow);
    }

    void createIdentityEdit(Pane container) {
        identityEditView = new IdentityEditView(identity);
        identityEditView.getView(v -> {
            Popup popup = new Popup(container, v, 300, 200);
            popup.show();
            identityEditView.getPresenter().onFinish(popup::close);
        });
        identityEditController = identityEditView.getPresenter();
    }

    public void show() {
        identityContextMenu.setVisible(true);
    }

    public void hide() {
        identityContextMenu.setVisible(true);
    }

    public boolean isVisible() {
        return identityContextMenu.isVisible();
    }
}
