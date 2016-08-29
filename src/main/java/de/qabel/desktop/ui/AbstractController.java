package de.qabel.desktop.ui;

import com.google.gson.Gson;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.crashReports.CrashReportHandler;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class AbstractController {
    @Inject
    private CrashReportHandler reportHandler;

    protected Gson gson;

    protected void alert(Exception e) {
        alert(e.getMessage(), e);
    }

    protected void tryOrAlert(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            alert(e);
        }
    }

    protected String getString(ResourceBundle resources, String message, Object... params) {
        return MessageFormat.format(resources.getString(message), params);
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    public CrashReportAlert alert;

    protected void alert(String message, Exception e) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> alert(message, e));
            return;
        }
        LoggerFactory.getLogger(getClass()).error(message, e);
        e.printStackTrace();

        CrashReportAlert alert = new CrashReportAlert(reportHandler, message, e);
        this.alert = alert;
        alert.showAndWait();
    }

    protected void writeStringInFile(String json, File dir) throws IOException {
        File targetFile = new File(dir.getPath());
        targetFile.createNewFile();
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(json.getBytes());
    }

    public String readFile(File f) throws IOException {
        FileReader fileReader = new FileReader(f);
        BufferedReader br = new BufferedReader(fileReader);

        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
                if (line != null) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }


    private ArrayList<DropURL> generateDropURLs(List<String> drops) throws URISyntaxException, QblDropInvalidURL {
        ArrayList<DropURL> collection = new ArrayList<>();

        for (String uri : drops) {
            DropURL dropURL = new DropURL(uri);

            collection.add(dropURL);
        }
        return collection;
    }

}
