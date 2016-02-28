package de.qabel.desktop.crashReports;

import java.io.IOException;
import java.net.URISyntaxException;

public interface CrashReportHandler {

	void sendFeedback(String feedbackFieldText, String name, String email) throws URISyntaxException, IOException;
	int sendStacktrace(String feedback, String stacktrace) throws URISyntaxException, IOException;

}
