package de.qabel.desktop.crashReports;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import de.qabel.desktop.hockeyapp.HockeyAppConfiguration;
import de.qabel.desktop.hockeyapp.HockeyAppVersion;
import de.qabel.desktop.hockeyapp.VersionClient;
import org.apache.http.NameValuePair;
import org.apache.http.entity.BasicHttpEntity;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HockeyAppTest {

    private final String shortVersion = "1.1";

    CloseableHttpClientStub httpClient = new CloseableHttpClientStub();

    HockeyAppConfiguration config = new HockeyAppConfiguration(shortVersion, httpClient);
    VersionClient versionClient = new VersionClient(config, httpClient);
    HockeyApp hockeyApp = new HockeyApp(shortVersion, httpClient);

    private String feedback;
    private String name;
    private String email;
    private String stacktrace;

    public HockeyAppTest() {
        versionClient.setVersion(new HockeyAppVersion(1, config.getAppVersion()));
    }

    @Before
    public void setUp() throws Exception {
        feedback = "HockeyAppTest feedback text";
        name = "HockeyAppTest";
        email = "HockeyAppTest@desktop.de";
        stacktrace = "XCEPTION REASON STRING\n" +
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

        stubFeedbackResponse();
        stubVersionToApp();
    }


    @Test
    public void testSendFeedback() throws IOException {

        hockeyApp.sendFeedback(feedback, name, email);
        String responseContent = getStubFeedbackResponseContent();
        JSONObject json = parseFeedbackJson(responseContent);

        assertEquals(123, json.get("id"));
        assertEquals(shortVersion, versionClient.getVersion().getShortVersion());
    }


    @Test
    public void testSendCrashReport() throws IOException {
        stubSendCrashReport();
        hockeyApp.sendStacktrace(feedback, stacktrace);
    }

    @Test
    public void createLog() throws IOException {
        String containingString = "Version: 1.1";
        stacktrace = hockeyApp.createLog(stacktrace);

        assertTrue(stacktrace.contains(containingString));
    }

    private void stubSendCrashReport() {
        String responseContent = "some foobar testing argh piares";

        String uri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/crashes/upload";
        CloseableHttpResponseStub response = this.createResponseFromString(200, responseContent);
        httpClient.addResponse("POST", uri, response);
    }

    JSONObject parseFeedbackJson(String responseContent) throws IOException {
        try {
            JSONObject parsedJson = new JSONObject(responseContent);
            return parsedJson.getJSONObject("feedback");
        } catch (JSONException e) {
            throw new IOException("returned JSON was invalid", e);
        }
    }

    @Test
    public void testBuildParams() throws IOException {

        List<NameValuePair> parameters = hockeyApp.buildFeedbackParams(feedback, name, email);

        String paramVersionKey = parameters.get(4).getName();
        String paramVersion = parameters.get(4).getValue();

        String expectedVersionID = "1";
        assertEquals("app_version_id", paramVersionKey);
        assertEquals(expectedVersionID, paramVersion);
    }

    private void stubVersionToApp() {
        HockeyAppVersion testVersion = new HockeyAppVersion(1, config.getAppVersion());
        hockeyApp.versionClient.setVersion(testVersion);
    }


    private void stubFeedbackResponse() {
        String responseContent = getStubFeedbackResponseContent();

        String uri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/feedback";
        CloseableHttpResponseStub response = this.createResponseFromString(200, responseContent);
        httpClient.addResponse("POST", uri, response);

    }

    @NotNull
    private String getStubFeedbackResponseContent() {
        return " {\n" +
            "  \"feedback\" : {\n" +
            "    \"created_at\" : \"2016-01-20T12:17:49Z\",\n" +
            "    \"id\" : 123,\n" +
            "    \"name\": \"Nermin Nicevic\",\n" +
            "    \"email\": \"nicevic@qabel.de\",\n" +
            "    \"token\" : \"" + config.getSecurityTokenKey() + "\",\n" +
            "    \"messages\" : [\n" +
            "      {\n" +
            "        \"clean_text\" : \"This starts a new thread.\",\n" +
            "        \"created_at\" : \"2016-01-20T12:17:49Z\",\n" +
            "        \"name\": \"Nermin Nicevic\",\n" +
            "        \"email\": \"nicevic@qabel.de\",\n" +

            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"status\" : \"success\",\n" +
            "  \"token\" : \"" + config.getSecurityTokenKey() + "\"\n" +
            "}";
    }

    private CloseableHttpResponseStub createResponseFromString(int statusCode, String responseContent) {
        CloseableHttpResponseStub response = new CloseableHttpResponseStub();
        response.setStatusCode(statusCode);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(responseContent.getBytes()));
        response.setEntity(entity);

        return response;
    }


}
