package de.qabel.desktop.hockeyapp;

import java.io.IOException;

public interface CrashesClient {
   void sendStacktrace(String feedback, String stacktrace) throws IOException;
}
