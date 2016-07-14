package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;

import java.io.IOException;
import java.util.Date;

public class HockeyCrashesClient implements CrashesClient {

    private HockeyAppRequestBuilder requestBuilder;
    private VersionClient versionClient;

    public HockeyCrashesClient(HockeyAppRequestBuilder requestBuilder, VersionClient versionClient) {
        this.requestBuilder = requestBuilder;
        this.versionClient = versionClient;
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        HttpPost request = requestBuilder.getHttpPost("/crashes/upload");

        String operatingSystemInformation = System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version");
        String manufacturer = System.getProperty("java.vendor");
        String model = System.getProperty("java.version");

        String log = createLog(stacktrace, new Date(), operatingSystemInformation, manufacturer, model);
        HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("log", new ByteArrayBody(log.getBytes(), "log"))
            .addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
            .build();
        request.setEntity(entity);

        requestBuilder.getHttpClient().execute(request);
    }

    String createLog(String stacktrace, Date now, String os, String manufacturer, String model) throws IOException {

        StringBuilder log = new StringBuilder();
        log.append("Package: de.qabel.desktop").append("\n");
        log.append("Version: ").append(versionClient.getVersion().getShortVersion()).append("\n");
        log.append("OS: ").append(os).append("\n");
        log.append("Manufacturer: ").append(manufacturer).append("\n");
        log.append("Model: ").append(model).append("\n");
        log.append("Date: ").append(now).append("\n");
        log.append("Stacktrace: ").append(stacktrace);

        return log.toString();
    }
}
