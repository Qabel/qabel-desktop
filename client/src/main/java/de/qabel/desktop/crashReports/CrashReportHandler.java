package de.qabel.desktop.crashReports;

import java.io.IOException;

public interface CrashReportHandler {

    void sendFeedback(String feedbackFieldText, String name, String email) throws IOException;

    void sendStacktrace(String feedback, String stacktrace) throws IOException;

}
