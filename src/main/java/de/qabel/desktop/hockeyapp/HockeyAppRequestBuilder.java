package de.qabel.desktop.hockeyapp;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class HockeyAppRequestBuilder {

    private final String API_BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private final String API_APP_KEY = "3b119dc227334d2d924e4e134c72aadc";

    private final String SECURITY_TOKEN_KEY = "350b097ef0964b17a0f3907050de309d";
    private final String SECURITY_TOKEN_NAME = "X-HockeyAppToken";

    private String appVersion;
    private HttpClient httpClient;

    public HockeyAppRequestBuilder(String appVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appVersion = appVersion;
    }

    HttpGet getHttpGet(String apiCallPath) {
        HttpGet request = new HttpGet(buildApiUri(apiCallPath));
        request.addHeader(getSecurityTokenName(), getSecurityTokenKey());
        return request;
    }

    HttpPost getHttpPost(String apiCallPath) {
        HttpPost request = new HttpPost(buildApiUri(apiCallPath));
        request.addHeader(getSecurityTokenName(), getSecurityTokenKey());
        return request;
    }

    void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    String getApiBaseUri() {
        return API_BASE_URI + API_APP_KEY;
    }

    String getSecurityTokenKey() {
        return SECURITY_TOKEN_KEY;
    }

    String getSecurityTokenName() {
        return SECURITY_TOKEN_NAME;
    }

    String buildApiUri(String apiCallPath) {
        return getApiBaseUri() + apiCallPath;
    }

    String getAppVersion() {
        return appVersion;
    }

    HttpClient getHttpClient() {
        return httpClient;
    }
}
