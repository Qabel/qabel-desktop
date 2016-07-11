package de.qabel.desktop.hockeyapp;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class HockeyAppConfiguration {

    private static final String API_BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private static final String API_APP_KEY = "3b119dc227334d2d924e4e134c72aadc";

    private static final String SECURITY_TOKEN_KEY = "350b097ef0964b17a0f3907050de309d";
    private static final String SECURITY_TOKEN_NAME = "X-HockeyAppToken";

    public String appVersion;
    private HttpClient httpClient = HttpClients.createMinimal();

    public HockeyAppConfiguration(String appVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appVersion = appVersion;
    }

    public static String getApiBaseUri() {
        return API_BASE_URI + API_APP_KEY;
    }

    public static String getApiAppKey() {
        return API_APP_KEY;
    }

    public static String getSecurityTokenKey() {
        return SECURITY_TOKEN_KEY;
    }

    public static String getSecurityTokenName() {
        return SECURITY_TOKEN_NAME;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
