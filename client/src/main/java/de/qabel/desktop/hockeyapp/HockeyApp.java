package de.qabel.desktop.hockeyapp;


import de.qabel.desktop.crashReports.CrashReportHandler;

import java.io.IOException;

public class HockeyApp implements CrashReportHandler {

    private FeedbackClient feedbackClient;
    private CrashReporterClient crashClient;

    public HockeyApp(FeedbackClient feedbackClient, CrashReporterClient crashClient) {
        this.feedbackClient = feedbackClient;
        this.crashClient = crashClient;
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
