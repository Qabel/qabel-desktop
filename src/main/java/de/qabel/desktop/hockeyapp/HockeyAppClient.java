package de.qabel.desktop.hockeyapp;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class HockeyAppClient {

    private static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";

    private static final String TOKEN_HEADERNAME = "350b097ef0964b17a0f3907050de309d";
    public String appVersion;
    private HttpClient httpClient = HttpClients.createMinimal();

    public HockeyAppClient(String appVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appVersion = appVersion;
    }

    public static String getAppId() {
        return APP_ID;
    }

    public static String getTokenHeaderName() {
        return TOKEN_HEADERNAME;
    }

    String buildApiUri(String apiCallPath){
        return getBaseUri() + apiCallPath;
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

    public String getAppVersion() {
        return appVersion;
    }


}
