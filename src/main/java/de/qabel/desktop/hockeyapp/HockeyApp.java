package de.qabel.desktop.hockeyapp;


import de.qabel.desktop.crashReports.CrashReportHandler;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeyApp implements CrashReportHandler {

    HockeyAppConfiguration config;
    VersionClient versionClient;
    HttpClient httpClient;

    public HockeyApp(String currentVersion, HttpClient httpClient) {
        this.config = new HockeyAppConfiguration(currentVersion, httpClient);
        this.httpClient = httpClient;
        this.versionClient = new VersionClient(config, httpClient);
    }


    @Override
    public void sendFeedback(String feedback, String name, String email) throws IOException {
        HttpPost request = config.getHttpPost("/feedback");
        List<NameValuePair> parameters = buildFeedbackParams(feedback, name, email);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        config.getHttpClient().execute(request);
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        HttpPost request = config.getHttpPost("/crashes/upload");

        String log = createLog(stacktrace).toString();

        HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("log", new ByteArrayBody(log.getBytes(), "log"))
            .addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
            .build();
        request.setEntity(entity);

        httpClient.execute(request);
    }

    List<NameValuePair> buildFeedbackParams(String feedback, String name, String email) throws IOException {
        HockeyAppVersion version = versionClient.getVersion();
        String versionId = "" + version.getVersionId();

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedback));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("app_version_id", versionId));

        return parameters;
    }


    String createLog(String stacktrace) throws IOException {
        Date date = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: de.qabel.desktop\n");
        log.append("Version: ").append(versionClient.getVersion().getShortVersion()).append("\n");
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
