package de.qabel.desktop.crashReports;


import de.qabel.desktop.hockeyapp.HockeyAppConfiguration;
import de.qabel.desktop.hockeyapp.HockeyAppVersion;
import de.qabel.desktop.hockeyapp.VersionClient;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeyApp implements CrashReportHandler {

    HockeyAppConfiguration config;
    VersionClient versionClient;

    public HockeyApp(String currentVersion) {
        this.config = new HockeyAppConfiguration(currentVersion);
        this.versionClient = new VersionClient(config);

    }


    @Override
    public void sendFeedback(String feedbackFieldText, String name, String email) throws IOException {
        HttpPost request = config.getHttpPost("/feedback");
        List<NameValuePair> parameters = buildFeedbackParams(feedbackFieldText, name, email);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        config.getHttpClient().execute(request);
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {

    }

    @NotNull
    List<NameValuePair> buildFeedbackParams(String feedback, String name, String email) throws IOException {
        versionClient.setUp(config.getAppVersion());

        HockeyAppVersion version = versionClient.getVersion();
        String versionId = "" + version.getVersionId();

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedback));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("app_version_id", versionId));

        return parameters;
    }


    private String createLog(String stacktrace) {
        Date date = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: de.qabel.desktop\n");
        log.append("Version: 1\n");
        log.append("OS: " + System.getProperty("os.name") + " / ");
        log.append(System.getProperty("os.arch") + " / ");
        log.append(System.getProperty("os.version") + "\n");
        log.append("Manufacturer: " + System.getProperty("java.vendor") + "\n");
        log.append("Model: " + System.getProperty("java.version") + "\n");
        log.append("Date: " + date + "\n");
        log.append("\n");
        log.append(stacktrace);

        return log.toString();
    }

}
