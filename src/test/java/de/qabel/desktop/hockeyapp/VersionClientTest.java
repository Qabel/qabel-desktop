package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.NameValuePair;
import org.apache.http.entity.BasicHttpEntity;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class VersionClientTest {

    private static final int VERSION_ID_1_0 = 195;
    private static final String VERSION_SHORT_1_0 = "1.0";

    private static final int VERSION_ID_1_1 = 208;
    private static final String VERSION_SHORT_1_1 = "1.1";

    private CloseableHttpClientStub httpClient = new CloseableHttpClientStub();
    private HockeyAppConfiguration config = new HockeyAppConfiguration(VERSION_SHORT_1_0, httpClient);
    private VersionClient client = new VersionClient(config, httpClient);

    @Test
    public void testFindVersion() throws IOException, VersionNotFoundException {
        loadFakeVersions();
        HockeyAppVersion version = client.findVersion(VERSION_SHORT_1_0);
        assertEquals(VERSION_SHORT_1_0, version.getShortVersion());
    }

    @Test
    public void createNewVersion() throws IOException {

        String shortVersion = "1.5";
        buildTestVersions();
        loadFakeCreatedVersionResponse(shortVersion);

        HockeyAppVersion version = client.createVersion(shortVersion);
        client.setVersion(version);

        assertEquals(shortVersion, client.getVersion().getShortVersion());
    }


    @Test(expected = IOException.class)
    public void parseInvalidVersionCreateResponse() throws IOException {
        String responseContent = "nbzuhbggzubzug";
        client.parseVersionCreateResponse(responseContent);
        client.createVersion("invalid");
    }

    @Test(expected = IOException.class)
    public void parseInvalidVersionsResponse() throws IOException {
        String responseContent = "nbzuhbggzubzug";
        client.parseVersionsResponse(responseContent);
    }

    @Test
    public void loadVersions() throws IOException, JSONException {
        loadFakeVersions();
        List<HockeyAppVersion> versions = client.getVersions();
        HockeyAppVersion version = versions.get(0);

        assertEquals(2, versions.size());
        assertEquals(VERSION_ID_1_0, version.getVersionId());
        assertEquals(VERSION_SHORT_1_0, version.getShortVersion());
    }

    @Test
    public void buildApiUri() {
        String testUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/somewhere";
        assertEquals(testUri, config.buildApiUri("/somewhere"));
    }

    private void buildTestVersions() {
        List<HockeyAppVersion> versions = client.getVersions();
        versions.add(new HockeyAppVersion(VERSION_ID_1_0, VERSION_SHORT_1_0));
        versions.add(new HockeyAppVersion(VERSION_ID_1_1, VERSION_SHORT_1_1));
        client.setVersions(versions);
    }

    private void loadFakeCreatedVersionResponse(String shortVersion) throws IOException {
        String responseContent = getVersionCreateResponseString(shortVersion);

        CloseableHttpResponseStub response = createResponseFromString(201, responseContent);
        String newVersionUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions/new";
        httpClient.addResponse("POST", newVersionUri, response);
    }

    @NotNull
    private String getVersionCreateResponseString(String shortVersion) {
        return "{\n" +
            "    \"id\": \"1337\",\n" +
            "    \"shortversion\": \"" + shortVersion + "\",\n" +
            "    \"title\": \"createNewVersion\",\n" +
            "    \"timestamp\": 1467877960,\n" +
            "    \"version\": \"23\",\n" +
            "}";
    }

    @Test
    public void buildParameters() {
        List<NameValuePair> params = client.buildCreateParameters(VERSION_SHORT_1_1);
        assertEquals(VERSION_SHORT_1_1, TestUtils.getValueByKey(params, "bundle_short_version"));
    }

    private void loadFakeVersions() throws IOException {
        String versionsResponseContent = getVersionsJsonString();

        String versionsUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        CloseableHttpResponseStub response = createResponseFromString(200, versionsResponseContent);
        httpClient.addResponse("GET", versionsUri, response);
        client.loadVersions();
    }

    @NotNull
    private String getVersionsJsonString() {
        return "{\n" +
            "    \"app_versions\": [\n" +
            "        {\n" +
            "            \"version\": \"" + VERSION_ID_1_0 + "\",\n" +
            "            \"shortversion\": \"" + VERSION_SHORT_1_0 + "\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/" + VERSION_ID_1_0 + "\",\n" +
            "            \"download_url\":\"https://rink.hockeyapp.net/apps/0873e2b98ad046a92c170a243a8515f6/app_versions/" + VERSION_ID_1_0 + "\",\n" +
            "            \"timestamp\": 1326195742,\n" +
            "            \"appsize\": 157547,\n" +
            "            \"device_family\": null,\n" +
            "            \"notes\": \"<p>Fixed bug when users could not sign in.</p>\\n\",\n" +
            "            \"status\": 2,\n" +
            "            \"minimum_os_version\": null,\n" +
            "            \"title\": \"HockeyApp\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"version\": \"" + VERSION_ID_1_1 + "\",\n" +
            "            \"shortversion\": \"" + VERSION_SHORT_1_1 + "\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/" + VERSION_ID_1_1 + "\",\n" +
            "            \"timestamp\": 1325597848,\n" +
            "            \"appsize\": 157591,\n" +
            "            \"device_family\": null,\n" +
            "            \"notes\": \"<ul>\\n<li>Added action bar with native support for Android 3.x and 4.0.</li>\\n<li>Added grid view on Android tablets.</li>\\n<li>Added &quot;Check for Updates&quot; to menu.</li>\\n<li>Changed layout of detail view.</li>\\n<li>Updated HockeySDK + various bug fixes.</li>\\n</ul>\\n\",\n" +
            "            \"status\": 1,\n" +
            "            \"minimum_os_version\": null,\n" +
            "            \"title\": \"HockeyApp\"\n" +
            "        },\n" +
            "\n" +
            "    ],\n" +
            "    \"status\": \"success\"\n" +
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
