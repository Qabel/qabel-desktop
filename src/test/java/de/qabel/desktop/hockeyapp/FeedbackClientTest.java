package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.NameValuePair;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class FeedbackClientTest {

    private CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();

    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder("https://rink.hockeyapp.net/api/2/apps/", "1.1", httpClientStub);

    private VersionClient versionClient = new VersionClient(requestBuilder);

    private HockeyFeedbackClient client = new HockeyFeedbackClient(requestBuilder, versionClient);

    private String feedback = "This is a fancy feedback from my Tests";
    private String name = "FeedbackClientTest";
    private String email = "FeedbackClientTest@example.de";

    @Before
    public void setUp() throws Exception {
        versionClient.setVersion(new HockeyAppVersion(requestBuilder.getAppVersion()));
    }

    @Test
    public void testBuildParams() throws Exception {

        String version = versionClient.getVersion().shortVersion;
        List<NameValuePair> params = client.buildParams(feedback, name, email);
        assertEquals(TestUtils.getValueByKey(params, "email"), email);
        assertEquals(TestUtils.getValueByKey(params, "bundle_version"), version);
    }

    @Test
    public void sendFeedback() throws IOException {
        stubFeedbackResponse();
        client.sendFeedback(feedback, name, email);
        assertNotNull(httpClientStub.getBody());
    }


    private void stubFeedbackResponse() {
        String responseContent = getStubFeedbackResponseContent();
        String uri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/feedback";
        CloseableHttpResponseStub response = TestUtils.createResponseFromString(200, responseContent);
        httpClientStub.addResponse("POST", uri, response);
    }

    @NotNull
    private String getStubFeedbackResponseContent() {
        return " {\n" +
            "  \"feedback\" : {\n" +
            "    \"created_at\" : \"2016-01-20T12:17:49Z\",\n" +
            "    \"id\" : 123,\n" +
            "    \"name\": \"Nermin Nicevic\",\n" +
            "    \"email\": \"nicevic@qabel.de\",\n" +
            "    \"token\" : \"" + requestBuilder.SECURITY_TOKEN_KEY + "\",\n" +
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
            "  \"token\" : \"" + requestBuilder.SECURITY_TOKEN_KEY + "\"\n" +
            "}";
    }

}
