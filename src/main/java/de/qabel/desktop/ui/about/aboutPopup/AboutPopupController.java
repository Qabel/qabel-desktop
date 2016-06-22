package de.qabel.desktop.ui.about.aboutPopup;

import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutPopupController extends AbstractController implements Initializable {

    @FXML
    private TextArea textAreaPopup;

    @FXML
    public Pane aboutPopup;

    @Inject
    private FilesAbout aboutFilesContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void buttonClosePopup() {
        hidePopup();
    }

    public void showQAPLPopup(){
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.QAPLContent);
        showPopup();

    }

    public void showThanksPopup() {
        setStyleTextAreaCenter();
        setTextAreaContent(aboutFilesContent.thanksFileContent);
        showPopup();
    }

    public void showImprintPopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.imprintContent);
        showPopup();

    }

    public void showTermsOfServicePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.termsOfServiceContent);
        showPopup();

    }

    public void showPrivacyNotesPopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.privateNotesContent);
        showPopup();
    }

    public void showApacheLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.apacheLicenseContent);
        showPopup();
    }

    public void showSilLicensePopup () {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.silLicenseContent);
        showPopup();
    }

    public void showLgplLicensePopup () {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.lgplLicenseContent);
        showPopup();
    }

    public void showCreativeLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.creativeLicenseContent);
        showPopup();
    }

    public void showAttributionLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.attributionLicenseContent);
        showPopup();
    }

    public void showEuropeanLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.europeanLicenseContent);
        showPopup();
    }

    public void showBouncyLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.bouncyLicenseContent);
        showPopup();
    }

    public void showEclipseLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.eclipseLicenseContent);
        showPopup();
    }

    public void showJsonLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.jsonLicenseContent);
        showPopup();
    }

    public void showBSDLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.bsdLicenseContent);
        showPopup();
    }

    public void showMITLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.mitLicenseContent);
        showPopup();
    }

    public void showInnoLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.innoLicenseContent);
        showPopup();
    }

    public void showLaunch4JLicensePopup() {
        setStyleTextArea();
        setTextAreaContent(aboutFilesContent.launch4jLicenseContent);
        showPopup();
    }

    public void setTextAreaContent (String content) {
        textAreaPopup.setText(content);
    }

    public String getTextAreaContent (){
        return textAreaPopup.getText();
    }

    public void hidePopup() {
        aboutPopup.setVisible(false);
    }

    public void showPopup() {
        aboutPopup.setVisible(true);
    }

    private void setStyleTextArea(){
        textAreaPopup.getStyleClass().clear();
        textAreaPopup.getStyleClass().add("text-area-popup");
    }

    private void setStyleTextAreaCenter(){
        textAreaPopup.getStyleClass().clear();
        textAreaPopup.getStyleClass().add("align-text-center");
    }
}
