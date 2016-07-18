package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;

import java.io.IOException;
import java.util.Date;

public class HockeyCrashReporterClient implements CrashReporterClient {

    private HockeyAppRequestBuilder requestBuilder;
    private VersionClient versionClient;

    public HockeyCrashReporterClient(HockeyAppRequestBuilder requestBuilder, VersionClient versionClient) {
        this.requestBuilder = requestBuilder;
        this.versionClient = versionClient;
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        HttpPost request = requestBuilder.preparePostRequest("/crashes/upload");

        String operatingSystemInformation = System.getProperty("os.name") + " / " + System.getProperty("os.arch")
            + " / " + System.getProperty("os.version");
        String manufacturer = System.getProperty("java.vendor");
        String model = System.getProperty("java.version");

        String log = createLog(stacktrace, new Date(), operatingSystemInformation, manufacturer, model);

        String version = versionClient.getVersion().shortVersion;

        HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("log", new ByteArrayBody(log.getBytes(), "log"))
            .addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
            .addPart("bundle_version", new ByteArrayBody(version.getBytes(), "bundle_version"))
            .addPart("bundle_short_version", new ByteArrayBody(version.getBytes(), "bundle_short_version"))
            .build();
        request.setEntity(entity);

        requestBuilder.getHttpClient().execute(request);
    }

    String createLog(String stacktrace, Date now, String os, String manufacturer, String model) throws IOException {
        HockeyAppVersion version = versionClient.getVersion();
        return "Package: de.qabel.desktop\n" +
            "Version: " + version.shortVersion + "\n" +
            "OS: " + os + "\n" +
            "Manufacturer: " + manufacturer + "\n" +
            "Model: " + model + "\n" +
            "Date: " + now + "\n" +
            "Stacktrace: " + stacktrace;
    }
}
