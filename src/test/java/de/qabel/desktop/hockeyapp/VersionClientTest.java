package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VersionClientTest {


    public static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    public static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    public static final String TOKEN = "350b097ef0964b17a0f3907050de309d";

    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();

    private HockeyAppClient hockeyAppClient = new HockeyAppClient("1.1", httpClientStub);
    private VersionClient client = new VersionClient(hockeyAppClient);

    @Test
    public void checkAppVersion(){
        assertEquals("1.1", client.appVersion);
    }

    @Test
    public void findVersion() throws VersionNotFoundException {

        String shortVersion = "1.1";
        buildTestVersions();

        client.findAndLoadVersion(shortVersion);
        assertEquals(shortVersion, client.getVersion().getShortVersion());
    }

    @Test(expected = IOException.class)
    public void createNewVersionWithInvalidJSONResponse() throws IOException {
        String responseContent  = "";

        CloseableHttpResponseStub response = this.createResponseFromString(201, responseContent);
        String newVersionUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions/new";
        httpClientStub.addResponse("POST", newVersionUri, response);

        client.createVersion("invalid");
    }

    @Test
    public void createNewVersion() throws IOException {

        String shortVersion = "1.1";
        try {
            client.findAndLoadVersion(shortVersion);
        } catch (VersionNotFoundException ignored) {
            createVersion(shortVersion);
        }
        assertEquals(shortVersion, client.getVersion().getShortVersion());
        assertEquals(1337, client.getVersion().getVersionId());
    }

    public void createVersion(String version) throws IOException {
        loadFakeCreatedVersionResponse();
        client.createVersion(version);
    }

    @Test
    public void loadVersions() throws IOException {
        loadFakeVersions();

        List<HockeyAppVersion> versions = client.getVersions();

        assertEquals(2, versions.size());

        assertEquals(208, versions.get(0).getVersionId());
        assertEquals(195, versions.get(1).getVersionId());

        assertEquals("1.1", versions.get(0).getShortVersion());
        assertEquals("1.0", versions.get(1).getShortVersion());
    }

    @Test
    public void buildApiUri(){
        String testUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/somewhere";
        assertEquals(testUri, client.buildApiUri("/somewhere"));
    }


    /**
     * builds a list of test HockeyAppVersion and save it through the client
     * 2 items with shortversions of "1.1", "2.0"
     */
    private void buildTestVersions() {
        List<HockeyAppVersion> versions = client.getVersions();
        versions.add(new HockeyAppVersion(1, "1.1"));
        versions.add(new HockeyAppVersion(2, "2.0"));
        client.setVersions(versions);
    }

    /**
     * expected shortversion is "1.1"
     * @throws IOException
     */
    private void loadFakeCreatedVersionResponse() throws IOException {
        String responseContent  = "{\n" +
            "    \"title\": \"createNewVersion\",\n" +
            "    \"timestamp\": 1467877960,\n" +
            "    \"id\": \"1337\",\n" +
            "    \"version\": \"23\",\n" +
            "    \"shortversion\": \"1.1\",\n" +
            "}";

        CloseableHttpResponseStub response = this.createResponseFromString(201, responseContent);
        String newVersionUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions/new";
        httpClientStub.addResponse("POST", newVersionUri, response);
    }

    private void loadFakeVersions() throws IOException {
        String versionsResponseContent = "{\n" +
            "    \"app_versions\": [\n" +
            "        {\n" +
            "            \"version\": \"208\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/208\",\n" +
            "            \"download_url\":\"https://rink.hockeyapp.net/apps/0873e2b98ad046a92c170a243a8515f6/app_versions/208\",\n" +
            "            \"timestamp\": 1326195742,\n" +
            "            \"appsize\": 157547,\n" +
            "            \"device_family\": null,\n" +
            "            \"notes\": \"<p>Fixed bug when users could not sign in.</p>\\n\",\n" +
            "            \"status\": 2,\n" +
            "            \"shortversion\": \"1.1\",\n" +
            "            \"minimum_os_version\": null,\n" +
            "            \"title\": \"HockeyApp\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"version\": \"195\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/195\",\n" +
            "            \"timestamp\": 1325597848,\n" +
            "            \"appsize\": 157591,\n" +
            "            \"device_family\": null,\n" +
            "            \"notes\": \"<ul>\\n<li>Added action bar with native support for Android 3.x and 4.0.</li>\\n<li>Added grid view on Android tablets.</li>\\n<li>Added &quot;Check for Updates&quot; to menu.</li>\\n<li>Changed layout of detail view.</li>\\n<li>Updated HockeySDK + various bug fixes.</li>\\n</ul>\\n\",\n" +
            "            \"status\": 1,\n" +
            "            \"shortversion\": \"1.0\",\n" +
            "            \"minimum_os_version\": null,\n" +
            "            \"title\": \"HockeyApp\"\n" +
            "        },\n" +
            "\n" +
            "    ],\n" +
            "    \"status\": \"success\"\n" +
            "}";

        String versionsUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        CloseableHttpResponseStub response = this.createResponseFromString(200, versionsResponseContent);
        httpClientStub.addResponse("GET", versionsUri, response);
        client.loadVersions();
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
