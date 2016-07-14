package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CrashesClientTest {

    private CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder("1.1", httpClientStub);
    private VersionClient versionClient = new VersionClient(requestBuilder);
    private HockeyCrashesClient client = new HockeyCrashesClient(requestBuilder, versionClient);
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

    private Date now = new Date();


    private String feedback = "this is just a String for the stacktrace";


    @Before
    public void setUp() throws Exception {
        versionClient.setVersion(new HockeyAppVersion(1, requestBuilder.getAppVersion()));
    }

    @Test
    public void createLog() throws IOException {

        String operatingSystemInformation = System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version");
        String manufacturer = System.getProperty("java.vendor");
        String model = System.getProperty("java.version");

        String expectedFormattedStacktrace = "Package: de.qabel.desktop\n" +
            "Version: 1.1\n" +
            "OS: " + operatingSystemInformation + "\n" +
            "Manufacturer: " + manufacturer + "\n" +
            "Model: " + model + "\n" +
            "Date: " + now + "\n" +
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

        String formattedStacktrace = client.createLog(stacktrace, now, operatingSystemInformation, manufacturer, model);
        assertEquals(expectedFormattedStacktrace, formattedStacktrace);
    }

    @Test
    public void sendCrash() throws IOException {
        stubCrashReport();
        client.sendStacktrace(feedback, stacktrace);
        assertNotNull(httpClientStub.getBody());
    }

    private void stubCrashReport() {
        String responseContent = "";
        String uri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/crashes/upload";
        CloseableHttpResponseStub response = TestUtils.createResponseFromString(200, responseContent);
        httpClientStub.addResponse("POST", uri, response);
    }


}
