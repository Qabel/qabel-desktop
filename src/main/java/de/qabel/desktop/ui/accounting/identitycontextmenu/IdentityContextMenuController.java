package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditView;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import de.qabel.desktop.ui.util.Popup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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
    Pane identityContextMenu;

    @FXML
    VBox contextMenu;

    private QRCodeView qrcodeView;
    public QRCodeController qrcodeController;

    IdentityEditView identityEditView;
    public IdentityEditController identityEditController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }

    public void closeMenu() {
        closeHandler.run();
    }

    private void createQrCodePopup(Pane container) {
        if (qrcodeView == null) {
            qrcodeView = new QRCodeView(identity);
            container.getChildren().add(qrcodeView.getView());
            qrcodeController = qrcodeView.getPresenter();
        }
    }

    public void openQRCode() {
        createQrCodePopup(layoutWindow);
        qrcodeController.show();
        closeMenu();
    }

    public void openIdentityEdit() {
        createIdentityEdit(layoutWindow);
        closeMenu();
    }

    void createIdentityEdit(Pane container) {
        identityEditView = new IdentityEditView(identity);
        identityEditView.getView(v -> {
            Popup popup = new Popup(container, v, 300, 180);
            popup.show();
            identityEditView.getPresenter().onFinish(popup::close);
        });
        identityEditController = identityEditView.getPresenter();
    }

    public void show() {
        identityContextMenu.setVisible(true);
    }

    public boolean isVisible() {
        return identityContextMenu.isVisible();
    }

    private Runnable closeHandler = () -> {};
    public void onClose(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }
}
