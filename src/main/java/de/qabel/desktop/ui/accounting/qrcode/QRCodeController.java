package de.qabel.desktop.ui.accounting.qrcode;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;


public class QRCodeController extends AbstractController implements Initializable {
    @FXML
    private Pane qrcode;

    @FXML
    private Label labelClient;

    @FXML
    private Label labelUrl;

    @FXML
    private Label labelKey;

    @FXML
    private ImageView imageQrCode;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private DropUrlGenerator dropUrlGenerator;

    @Inject
    private URI accountingUri;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void buttonClosePopup() {
        hidePopup();
    }

    public void showQRCodePopup() {
        qrcode.setVisible(true);
    }

    private void hidePopup() {
        qrcode.setVisible(false);
    }

    public void setIdentity(Identity identity) {
        String textQRCode = "QABELCONTACT\n"
            + identity.getAlias() + "\n"
            + dropUrlGenerator.generateUrl().getUri().toString() + "\n"
            + identity.getEcPublicKey().getReadableKeyIdentifier();

        labelClient.setText(identity.getAlias());
        labelUrl.setText(dropUrlGenerator.generateUrl().getUri().toString());
        labelKey.setText(identity.getEcPublicKey().getReadableKeyIdentifier());
        generateQRCode(textQRCode);
    }

    private void generateQRCode(String qrcode) {
        final byte[] imageBytes;
        QRCode.from(qrcode).withErrorCorrection(ErrorCorrectionLevel.L);
        imageBytes = QRCode.from(qrcode).withSize(300, 250).to(ImageType.PNG).stream().toByteArray();
        Image qrCodeGraphics = new Image(new ByteArrayInputStream(imageBytes));
        imageQrCode.setImage(qrCodeGraphics);
    }
}
