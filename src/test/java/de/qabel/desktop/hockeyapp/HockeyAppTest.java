package de.qabel.desktop.hockeyapp;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class HockeyAppTest {

    private String feedbackText = "feedbackText - " + new Date();
    private String name = "Hockey App Test";
    private String email = "HockeyAppTest@who.is";
    private String stacktrace = "HockeyAppTest stacktrace";

    @Test
    public void sendFeedback() throws IOException {
        FeedbackClient feedback = (feedback1, name1, email1) -> {
            assertEquals(feedbackText, feedback1);
            assertEquals(name1, name1);
            assertEquals(email1, email1);
        };
        HockeyApp app = new HockeyApp(feedback, null);
        app.sendFeedback(feedbackText, name, email);
    }

    @Test
    public void sendCrashReport() throws IOException {
        CrashReporterClient crash = (feedback, stacktrace1) -> {
            assertEquals(feedbackText, feedback);
            assertEquals(stacktrace1, stacktrace1);
        };
        HockeyApp app = new HockeyApp(null, crash);
        app.sendStacktrace(feedbackText, stacktrace);
    }
}

