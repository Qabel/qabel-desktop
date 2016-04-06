package de.qabel.desktop.crashReports;

import java.io.IOException;
import java.net.URISyntaxException;

public class StubCrashReportHandler implements CrashReportHandler {
    @Override
    public void sendFeedback(String feedbackFieldText, String name, String email) throws IOException {
        return;
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
    }
}
