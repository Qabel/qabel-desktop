package de.qabel.desktop.crashReports;

import java.io.IOException;
import java.net.URISyntaxException;

public class StubCrashReportHandler implements CrashReportHandler {
    public String text;
    public String name;
    public String email;
    public String stacktrace;

    @Override
    public void sendFeedback(String feedbackFieldText, String name, String email) throws IOException {
        text = feedbackFieldText;
        this.name = name;
        this.email = email;
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        text = feedback;
        this.stacktrace = stacktrace;
    }
}
