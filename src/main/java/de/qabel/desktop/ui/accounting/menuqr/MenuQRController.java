package de.qabel.desktop.ui.accounting.menuqr;

import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuQRController extends AbstractController implements Initializable {

    @Inject
    private Pane layoutWindow;

    @FXML
    private AnchorPane menuQR;

    @FXML
    private VBox vboxMenu;

    private QRCodeView qrcodeView;
    private QRCodeController qrcodeController;
    private PopOver popOver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeQRPopup();
        initializePopOver();
    }

    private void initializeQRPopup() {
        qrcodeView = new QRCodeView();
        qrcodeView.getView(layoutWindow.getChildren()::add);
        qrcodeController = (QRCodeController) qrcodeView.getPresenter();
    }

    public void showMenu(Identity identity, double coordPopOverX, double coordPopOverY) {
        popOver.show(menuQR, coordPopOverX, coordPopOverY);
        qrcodeController.setIdentity(identity);
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
    }

    public void openQRCode() {
        closeMenu();
        qrcodeController.showQRCodePopup();
    }
}
