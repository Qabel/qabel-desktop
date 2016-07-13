package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class CrashesClientTest {

    private CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder("1.1", httpClientStub);
    private VersionClient versionClient = new VersionClient(requestBuilder);
    ;
    private CrashesClient client = new CrashesClient(requestBuilder, versionClient);
    private String stacktrace = "XCEPTION REASON STRING\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "ANOTHER EXCEPTION REASON STRING\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)";

    private String expectedFormattedStacktrace = "Package: de.qabel.desktop\n" +
        "Version: 1.1\n" +
        "OS: Linux / amd64 / 4.4.0-21-generic\n" +
        "Manufacturer: Oracle Corporation\n" +
        "Model: 1.8.0_91\n" +
        "Date: " + new Date() + "\n" +
        "Stacktrace: XCEPTION REASON STRING\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "ANOTHER EXCEPTION REASON STRING\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)\n" +
        "  at CLASS.METHOD(FILE:LINE)";
    private String feedback = "this is just a String for the stacktrace";

    @Before
    public void setUp() throws Exception {
        versionClient.setVersion(new HockeyAppVersion(1, requestBuilder.getAppVersion()));
    }

    @Test
    public void createLog() throws IOException {
        String formattedStacktrace = client.createLog(stacktrace);
        assertEquals(expectedFormattedStacktrace, formattedStacktrace);
    }

    @Test
    public void sendCrash() throws IOException {
        stubCrashReport();
        HttpResponse response = client.sendStacktrace(feedback, stacktrace);
        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);
    }

    private void stubCrashReport() {
        String responseContent = "";
        String uri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/crashes/upload";
        CloseableHttpResponseStub response = TestUtils.createResponseFromString(200, responseContent);
        httpClientStub.addResponse("POST", uri, response);
    }


}