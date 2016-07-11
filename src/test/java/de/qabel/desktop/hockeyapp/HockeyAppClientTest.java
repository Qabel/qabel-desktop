package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.desktop.crashReports.CrashReportHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class HockeyAppClientTest implements CrashReportHandler {

    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    HockeyAppClient client = new HockeyAppClient("1.1", httpClientStub);

    VersionClient versionClient = new VersionClient(client);

    @Test
    public void testFeedback() throws IOException {
        this.sendFeedback("this is my feedbacktext", "nnice", "someone@foo.de");
    }

    @Override
    public void sendFeedback(String feedbackFieldText, String name, String email) throws IOException {

        HttpPost httpPost = new HttpPost(client.buildApiUri("/feedback"));
        httpPost.addHeader("X-HockeyAppToken", client.getSecurityToken());

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedbackFieldText));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));
        httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        httpClientStub.execute(httpPost);
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {
        // /crashes/upload
    }
}
