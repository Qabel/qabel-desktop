package de.qabel.desktop.config;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.desktop.ui.AbstractController;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public class FilesAbout extends AbstractController {
    public String QAPLContent;
    public String thanksFileContent;
    public String imprintContent;
    public String termsOfServiceContent;
    public String privateNotesContent;
    public String apacheLicenseContent;
    public String silLicenseContent;
    public String attributionLicenseContent;
    public String lgplLicenseContent;
    public String creativeLicenseContent;
    private ResourceBundle resources;

    public FilesAbout(){
        resources = QabelFXMLView.getDefaultResourceBundle();
        try {
            QAPLContent = readFile(resources.getString("qaplPath"));
            thanksFileContent = readFile(resources.getString("thanksFilePath"));
            imprintContent = readFile(resources.getString("imprintPath"));
            termsOfServiceContent = readFile(resources.getString("termsOfServicePath"));
            privateNotesContent = readFile(resources.getString("privateNotesPath"));
            apacheLicenseContent = readFile(resources.getString("apacheLicensePath"));
            silLicenseContent = readFile(resources.getString("silLicensePath"));
            attributionLicenseContent = readFile(resources.getString("attributionLicensePath"));
            lgplLicenseContent = readFile(resources.getString("lgplLicensePath"));
            creativeLicenseContent = readFile(resources.getString("creativeLicensePath"));
        } catch (IOException e) {
            alert("failed to load about files", e);
        } catch (NullPointerException ignored) {
        }
    }

    private String readFile(String filePath) throws IOException {
        try (InputStream file = System.class.getResourceAsStream(filePath)) {
            return IOUtils.toString(file, "UTF-8");
        } catch (NullPointerException ignored) {
        }
        return Strings.EMPTY;
    }
}
