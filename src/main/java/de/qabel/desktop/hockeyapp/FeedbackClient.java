package de.qabel.desktop.hockeyapp;

import java.io.IOException;

public interface FeedbackClient {
    void sendFeedback(String feedback, String name, String email) throws IOException;
}
