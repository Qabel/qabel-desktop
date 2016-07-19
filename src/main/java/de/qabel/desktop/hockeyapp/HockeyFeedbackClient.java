package de.qabel.desktop.hockeyapp;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HockeyFeedbackClient implements FeedbackClient {

    private final VersionClient versionClient;
    private final HockeyAppRequestBuilder requestBuilder;

    public HockeyFeedbackClient(HockeyAppRequestBuilder requestBuilder, VersionClient versionClient) {
        this.requestBuilder = requestBuilder;
        this.versionClient = versionClient;
    }

    @Override
    public void sendFeedback(String feedback, String name, String email) throws IOException {
        HttpPost request = requestBuilder.preparePostRequest("/feedback");
        List<NameValuePair> parameters = buildParams(feedback, name, email);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        requestBuilder.getHttpClient().execute(request);
    }

    List<NameValuePair> buildParams(String feedback, String name, String email) throws IOException {
        HockeyAppVersion version = versionClient.getVersion();

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedback));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("bundle_version", version.shortVersion));
        parameters.add(new BasicNameValuePair("bundle_short_version", version.shortVersion));

        return parameters;
    }
}
