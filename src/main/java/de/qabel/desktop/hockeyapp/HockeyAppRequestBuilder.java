package de.qabel.desktop.hockeyapp;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class HockeyAppRequestBuilder {

    private final String API_BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private final String API_APP_KEY = "3b119dc227334d2d924e4e134c72aadc";

    final String SECURITY_TOKEN_KEY = "350b097ef0964b17a0f3907050de309d";
    final String SECURITY_TOKEN_NAME = "X-HockeyAppToken";

    private String appVersion;
    private HttpClient httpClient;

    public HockeyAppRequestBuilder(String appVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appVersion = appVersion;
    }

    HttpGet prepareGetRequest(String apiCallPath) {
        HttpGet request = new HttpGet(buildApiUri(apiCallPath));
        request.addHeader(SECURITY_TOKEN_NAME, SECURITY_TOKEN_KEY);
        return request;
    }

    HttpPost preparePostRequest(String apiCallPath) {
        HttpPost request = new HttpPost(buildApiUri(apiCallPath));
        request.addHeader(SECURITY_TOKEN_NAME, SECURITY_TOKEN_KEY);
        return request;
    }

    String getAppVersion() {
        return appVersion;
    }

    HttpClient getHttpClient() {
        return httpClient;
    }

    String buildApiUri(String apiCallPath) {
        return API_BASE_URI + API_APP_KEY + apiCallPath;
    }

}
