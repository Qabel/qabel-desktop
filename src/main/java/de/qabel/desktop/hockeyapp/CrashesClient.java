package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;

import java.io.IOException;
import java.util.Date;

public class CrashesClient {

    private HockeyAppRequestBuilder requestBuilder;
    private VersionClient versionClient;

    public CrashesClient(HockeyAppRequestBuilder requestBuilder, VersionClient versionClient) {
        this.requestBuilder = requestBuilder;
        this.versionClient = versionClient;
    }

    public HttpResponse sendStacktrace(String feedback, String stacktrace) throws IOException {
        HttpPost request = requestBuilder.getHttpPost("/crashes/upload");
        String log = createLog(stacktrace);
        HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("log", new ByteArrayBody(log.getBytes(), "log"))
            .addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
            .build();
        request.setEntity(entity);

        return requestBuilder.getHttpClient().execute(request);
    }

    String createLog(String stacktrace) throws IOException {
        Date date = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: de.qabel.desktop").append("\n");
        log.append("Version: ").append(versionClient.getVersion().getShortVersion()).append("\n");
        log.append("OS: ")
            .append(System.getProperty("os.name")).append(" / ")
            .append(System.getProperty("os.arch")).append(" / ")
            .append(System.getProperty("os.version"))
            .append("\n");
        log.append("Manufacturer: ").append(System.getProperty("java.vendor")).append("\n");
        log.append("Model: ").append(System.getProperty("java.version")).append("\n");
        log.append("Date: ").append(date).append("\n");
        log.append("Stacktrace: ").append(stacktrace);

        return log.toString();
    }
}