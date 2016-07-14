package de.qabel.desktop.hockeyapp;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HockeyAppTest {

    private String feedbackText = "feedbackText";
    private String name = "name";
    private String email = "name@who.is";
    private String stacktrace = "stacktrace";

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
        CrashesClient crash = (feedback, stacktrace1) -> {
            assertEquals(feedbackText, feedback);
            assertEquals(stacktrace1, stacktrace1);
        };
        HockeyApp app = new HockeyApp(null, crash);
        app.sendStacktrace(feedbackText, stacktrace);
    }
}

