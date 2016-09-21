package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;

import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;

import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import de.qabel.desktop.ui.contact.menu.ContactMenuController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    private Pane layoutWindow;

    @FXML
    public AnchorPane contextMenu;

    @FXML
    VBox vboxMenu;

    @FXML
    Button editButton;
    @FXML
    Button removeButton;
    @FXML
    Button exportButton;
    @FXML
    Button privateKeyButton;
    @FXML
    Button publicKeyQRButton;
    @FXML
    Button publicKeyEmailButton;

    @Inject
    private IdentityRepository identityRepository;

    private TextInputDialog dialog;

    private static ImageView editImageView = setImageView(loadImage("/img/pencil.png"));
    private static ImageView deleteImageView = setImageView(loadImage("/img/delete.png"));
    private static ImageView uploadImageView = setImageView(loadImage("/img/upload.png"));
    private static ImageView privateKeyImageView = setImageView(loadImage("/img/qrcode.png"));
    private static ImageView qrcodeImageView = setImageView(loadImage("/img/qrcode.png"));
    private static ImageView emailImageView = setImageView(loadImage("/img/email.png"));

    private QRCodeView qrcodeView;
    private QRCodeController qrcodeController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        createButtonsGraphics();
        createButtonsTooltip();
        eventHandlerOpenQRPopup();
    }

    private void eventHandlerOpenQRPopup() {
        privateKeyButton.setOnAction(event -> {
            ((Node) (event.getSource())).getScene().getWindow().hide();
            openQRCode();
        });
    }

    private static Image loadImage(String resourcePath) {
        return new Image(ContactMenuController.class.getResourceAsStream(resourcePath), 32, 32, true, true);
    }

    private static ImageView setImageView(Image image) {
        return new ImageView(image);
    }

    private void createButtonsGraphics() {
        editButton.setGraphic(editImageView);
        removeButton.setGraphic(deleteImageView);
        exportButton.setGraphic(uploadImageView);
        privateKeyButton.setGraphic(privateKeyImageView);
        publicKeyQRButton.setGraphic(qrcodeImageView);
        publicKeyEmailButton.setGraphic(emailImageView);
    }

    private void createButtonsTooltip() {
        Tooltip.install(editButton, new Tooltip(resourceBundle.getString("editDetails")));
        Tooltip.install(removeButton, new Tooltip(resourceBundle.getString("removeIdentity")));
        Tooltip.install(exportButton, new Tooltip(resourceBundle.getString("exportIdentityQID")));
        Tooltip.install(privateKeyButton, new Tooltip(resourceBundle.getString("exportPrivateKeyQR")));
        Tooltip.install(publicKeyQRButton, new Tooltip(resourceBundle.getString("sharePublicKeyQR")));
        Tooltip.install(publicKeyEmailButton, new Tooltip(resourceBundle.getString("sharePublicKeyEmail")));
    }

    private void initializeQRPopup() {
        if (qrcodeView == null) {
            final Map<String, Object> injectionContext = new HashMap<>();
            injectionContext.put("identity", identity);
            qrcodeView = new QRCodeView(injectionContext::get);
            qrcodeView.getView(layoutWindow.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void openMenu() {
        contextMenu.setVisible(true);
    }

    public void openQRCode() {
        initializeQRPopup();
        Platform.runLater(() -> qrcodeController.showPopup());
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

    public void setAlias(String alias) {
        identity.setAlias(alias);
        try {
            identityRepository.save(identity);
        } catch (PersistenceException e) {
            alert("Failed to save identity", e);
        }
    }

    public String getAlias() {
        return identity.getAlias();
    }
}
