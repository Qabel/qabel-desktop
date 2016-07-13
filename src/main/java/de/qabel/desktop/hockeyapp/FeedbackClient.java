package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FeedbackClient {

    private final VersionClient versionClient;
    private final HockeyAppRequestBuilder requestBuilder;

    public FeedbackClient(HockeyAppRequestBuilder requestBuilder, VersionClient versionClient) {
        this.requestBuilder = requestBuilder;
        this.versionClient = versionClient;
    }


    public HttpResponse sendFeedback(String feedback, String name, String email) throws IOException {
        HttpPost request = requestBuilder.getHttpPost("/feedback");
        List<NameValuePair> parameters = buildParams(feedback, name, email);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        return requestBuilder.getHttpClient().execute(request);
    }

    List<NameValuePair> buildParams(String feedback, String name, String email) throws IOException {
        HockeyAppVersion version = versionClient.getVersion();

        String versionId = "" + version.getVersionId();

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedback));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("app_version_id", versionId));

        return parameters;
    }

    public HockeyAppRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public VersionClient getVersionClient() {
        return versionClient;
    }
}
