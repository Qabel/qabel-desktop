package de.qabel.desktop.crashReports;

import java.io.IOException;
import java.net.URISyntaxException;

public class StubCrashReportHandler implements CrashReportHandler {
	@Override
	public int sendFeedback(String feedbackFieldText, String text, String feedback) throws URISyntaxException, IOException {
		return 201;
	}

	@Override
	public int sendStacktrace(String feedback, String stacktrace) throws URISyntaxException, IOException {
		return 201;
	}
}
