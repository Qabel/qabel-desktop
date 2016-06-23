package de.qabel.desktop.ui.about;

import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.about.aboutPopup.AboutPopupController;
import de.qabel.desktop.ui.about.aboutPopup.AboutPopupView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;

public class AboutController extends AbstractController implements Initializable {
    private static final String LICENSE_DEPENDENCY_MAPPING_LOCATION = "/license-dependency.xml";
    private static final String DEPENDENCY_LICENSE_MAPPING_LOCATION = "/dependency-license.xml";

    @FXML
    private Pane linkContainer;

    @FXML
    private Label appVersion;

    @FXML
    private Pane QAPLBox;

    @FXML
    private Pane thanksBox;

    @FXML
    private Pane termsOfServiceBox;

    @FXML
    private Pane imprintBox;

    @FXML
    private Pane privacyNotesBox;

    @FXML
    private Pane silLicenseBox;

    @FXML
    private Pane attributionLicenseBox;

    @FXML
    private Pane lgplLicenseBox;

    @FXML
    private Pane creativeLicenseBox;

    @FXML
    private Pane innoBox;

    @FXML
    private Pane launch4jBox;

    @FXML
    private Pane sourceSansBox;

    @Inject
    private Pane layoutWindow;

    @Inject
    private String currentVersion;

    @Inject
    private FilesAbout aboutFilesContent;

    public AboutPopupController popupController;
    private AboutPopupView popupView;

    private Map<String, String> jarNames = new HashMap<>();
    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        try {
            addDependencyLicenses();
            activateButtons();
            initializePopup();
            setDesktopVersion();
        } catch (Exception e) {
            alert("failed to load about contents: " + e.getMessage(), e);
        }
    }

    private void addDependencyLicenses() throws ParserConfigurationException, IOException, SAXException {
        indexJarNames();
        addLicenses();
    }

    private void addLicenses() throws ParserConfigurationException, IOException, SAXException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(getClass().getResourceAsStream(LICENSE_DEPENDENCY_MAPPING_LOCATION));
        doc.getDocumentElement().normalize();

        NodeList licenses = doc.getElementsByTagName("license");
        for (int i = 0; i < licenses.getLength(); i++) {
            org.w3c.dom.Node license = licenses.item(i);
            String licenseName = license.getAttributes().getNamedItem("name").getNodeValue();
            String licenseLink = license.getAttributes().getNamedItem("url").getNodeValue();
            List<String> dependencies = new LinkedList<>();

            NodeList childNodes = license.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                org.w3c.dom.Node child = childNodes.item(j);
                if (!child.getNodeName().equals("dependency")) {
                    continue;
                }

                String jarName = child.getTextContent();
                String dependencyName = jarName;
                if (jarNames.containsKey(jarName)) {
                    dependencyName = jarNames.get(jarName);
                }
                dependencies.add(dependencyName);
            }

            linkContainer.getChildren().add(createLabeledLink(licenseName, licenseLink, dependencies));
        }
    }

    private void indexJarNames() throws ParserConfigurationException, IOException, SAXException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(getClass().getResourceAsStream(DEPENDENCY_LICENSE_MAPPING_LOCATION));
        doc.getDocumentElement().normalize();

        NodeList deps = doc.getElementsByTagName("dependency");
        for (int i = 0; i < deps.getLength(); i++) {
            org.w3c.dom.Node dep = deps.item(i);
            NodeList childNodes = dep.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                org.w3c.dom.Node child = childNodes.item(j);
                if (child.getNodeName().equals("file")) {
                    jarNames.put(child.getTextContent(), dep.getAttributes().getNamedItem("name").getNodeValue());
                }
            }
        }
    }

    private void setDesktopVersion() {
        appVersion.setText(appVersion.getText() + " " + currentVersion);

    }

    private void activateButtons() {
        if (aboutFilesContent.QAPLContent.isEmpty()) {
            QAPLBox.setDisable(true);
        }
        if (aboutFilesContent.imprintContent.isEmpty()) {
            imprintBox.setDisable(true);
        }
        if (aboutFilesContent.silLicenseContent.isEmpty()) {
            silLicenseBox.setDisable(true);
            sourceSansBox.setDisable(true);
        }
        if (aboutFilesContent.thanksFileContent.isEmpty()) {
            thanksBox.setDisable(true);
        }
        if (aboutFilesContent.attributionLicenseContent.isEmpty()) {
            attributionLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.creativeLicenseContent.isEmpty()) {
            creativeLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.lgplLicenseContent.isEmpty()) {
            lgplLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.privateNotesContent.isEmpty()) {
            privacyNotesBox.setDisable(true);
        }
        if (aboutFilesContent.termsOfServiceContent.isEmpty()) {
            termsOfServiceBox.setDisable(true);
        }
        if (aboutFilesContent.innoLicenseContent.isEmpty()) {
            innoBox.setDisable(true);
        }
        if (aboutFilesContent.launch4jLicenseContent.isEmpty()) {
            launch4jBox.setDisable(true);
        }

    }

    private void initializePopup() {
        popupView = new AboutPopupView();
        popupView.getView(layoutWindow.getChildren()::add);
        popupController = (AboutPopupController) popupView.getPresenter();
    }

    private VBox createLabeledLink(String licenseName, String url, List<String> comments) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(0.5);

        Label label = new Label(getString(resourceBundle, "licensedComponents", licenseName));
        label.getStyleClass().add("label-title-box");
        vbox.getChildren().add(label);

        for (String comment : comments) {
            Label commentLabel = new Label(comment);
            vbox.getChildren().add(commentLabel);
        }

        StackPane stackPane = new StackPane();
        Hyperlink hyperlink = new Hyperlink(url);
        stackPane.setAlignment(Pos.BOTTOM_LEFT);
        stackPane.setMargin(hyperlink, new Insets(10, 0, 10, 10));
        hyperlink.getStyleClass().add("hyperlink-box-about");

        StringTokenizer name = new StringTokenizer(licenseName);
        String firstName = name.nextToken();
        Button buttonLicense = setActionButton(firstName);

        stackPane.getStyleClass().add("box-button-about");
        stackPane.getChildren().add(hyperlink);
        stackPane.getChildren().add(buttonLicense);
        stackPane.setDisable(disableVBox(firstName));
        vbox.getChildren().add(stackPane);

        return vbox;
    }

    private Boolean disableVBox(String licenseName) {
        Boolean disableVbox = false;

        switch (licenseName) {
            case "European":
                if (aboutFilesContent.europeanLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "Bouncy":
                if (aboutFilesContent.bouncyLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "Eclipse":
                if (aboutFilesContent.eclipseLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "Apache":
                if (aboutFilesContent.apacheLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "provided":
                if (aboutFilesContent.jsonLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "MIT":
                if (aboutFilesContent.mitLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            case "BSD":
                if (aboutFilesContent.bsdLicenseContent.isEmpty()) {
                    disableVbox = true;
                }
                break;
            default:
                disableVbox = true;
                break;
        }

        return disableVbox;
    }

    private Button setActionButton(String licenseName) {
        Button button = new Button();

        button.setText("Show License");
        button.getStyleClass().add("button-about");

        switch (licenseName) {
            case "European":
                button.onActionProperty().setValue(event -> {
                    openEuropeanLicensePopUp();
                });
                break;
            case "Apache":
                button.onActionProperty().setValue(event -> {
                    openApacheLicensePopUp();
                });
                break;
            case "Bouncy":
                button.onActionProperty().setValue(event -> {
                    openBouncyLicensePopUp();
                });
                break;
            case "Eclipse":
                button.onActionProperty().setValue(event -> {
                    openEclipseLicensePopUp();
                });
                break;
            case "provided":
                button.onActionProperty().setValue(event -> {
                    openJsonLicensePopUp();
                });
                break;
            case "MIT":
                button.onActionProperty().setValue(event -> {
                    openMITLicensePopUp();
                });
                break;
            case "BSD":
                button.onActionProperty().setValue(event -> {
                    openBSDLicensePopUp();
                });
                break;
            default:
                alert("The license was not found" + licenseName, new Exception("Legal Exception"));
                break;
        }

        return button;
    }

    public void openThanksPopUp() {
        popupController.showThanksPopup();
    }

    public void openQAPLPopUp() {
        popupController.showQAPLPopup();
    }

    public void openImprintPopUp() {
        popupController.showImprintPopup();
    }

    public void openTermsOfServicePopUp() {
        popupController.showTermsOfServicePopup();
    }

    public void openPrivacyPopUp() {
        popupController.showPrivacyNotesPopup();
    }

    public void openApacheLicensePopUp() {
        popupController.showApacheLicensePopup();
    }

    public void openSilPopUp() {
        popupController.showSilLicensePopup();
    }

    public void openAttributionLicensePopUp() {
        popupController.showAttributionLicensePopup();
    }

    public void openLGPLLicensePopUp() {
        popupController.showLgplLicensePopup();
    }

    public void openCreativeLicensePopUp() {
        popupController.showCreativeLicensePopup();
    }

    public void openEuropeanLicensePopUp() {
        popupController.showEuropeanLicensePopup();
    }

    public void openBouncyLicensePopUp() {
        popupController.showBouncyLicensePopup();
    }

    public void openEclipseLicensePopUp() {
        popupController.showEclipseLicensePopup();
    }

    public void openJsonLicensePopUp() {
        popupController.showJsonLicensePopup();
    }

    public void openMITLicensePopUp() {
        popupController.showMITLicensePopup();
    }

    public void openBSDLicensePopUp() {
        popupController.showBSDLicensePopup();
    }

    public void openInnoLicensePopUp() {
        popupController.showInnoLicensePopup();
    }

    public void openLaunch4jLicensePopUp() {
        popupController.showLaunch4JLicensePopup();
    }
}
