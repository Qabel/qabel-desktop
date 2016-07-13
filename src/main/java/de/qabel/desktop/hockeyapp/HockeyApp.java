package de.qabel.desktop.hockeyapp;


import de.qabel.desktop.crashReports.CrashReportHandler;
import org.apache.http.client.HttpClient;

import java.io.IOException;

public class HockeyApp implements CrashReportHandler {

    private final FeedbackClient feedbackClient;
    private final HockeyAppRequestBuilder requestBuilder;
    private final VersionClient versionClient;
    private final CrashesClient crashClient;

    public HockeyApp(String currentVersion, HttpClient httpClient) {
        this.requestBuilder = new HockeyAppRequestBuilder(currentVersion, httpClient);

        this.versionClient = new VersionClient(requestBuilder);
        this.feedbackClient = new FeedbackClient(requestBuilder, versionClient);
        this.crashClient = new CrashesClient(requestBuilder, versionClient);
    }

    @Override
    public void sendFeedback(String feedback, String name, String email) throws IOException {
        feedbackClient.sendFeedback(feedback, name, email);
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        crashClient.sendStacktrace(feedback, stacktrace);
    }

}
