package de.qabel.desktop.hockeyapp;

import de.qabel.desktop.crashReports.CrashReportHandler;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.List;

public class HockeyAppClient implements CrashReportHandler {

    private static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";
    private HttpClient httpClient;

    public List<HockeyAppVersion> versions;
    public String appVersion;

    public HockeyAppClient(String appVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appVersion = appVersion;
    }

    @Override
    public void sendFeedback(String feedback, String name, String email) throws IOException {

    }


    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {

    }

    private String createLog(String stacktrace) {
        return "";
    }


    String buildApiUri(String apiCallPath){
        return BASE_URI + APP_ID + apiCallPath;
    }



    public String getBaseUri() {
        return BASE_URI + APP_ID;
    }

    public String getSecurityToken() {
        return TOKEN;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public static String getAppId() {
        return APP_ID;
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
