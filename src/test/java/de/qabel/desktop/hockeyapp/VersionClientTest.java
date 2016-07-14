package de.qabel.desktop.hockeyapp;

import com.google.gson.JsonObject;
import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.NameValuePair;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VersionClientTest {

    private static final int VERSION_ID_1_0 = 195;
    private static final String VERSION_SHORT_1_0 = "1.0";

    private static final int VERSION_ID_1_1 = 208;
    private static final String VERSION_SHORT_1_1 = "1.1";

    private CloseableHttpClientStub httpClient = new CloseableHttpClientStub();
    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder(VERSION_SHORT_1_0, httpClient);
    private VersionClient client = new VersionClient(requestBuilder);

    @Test
    public void validFindVersion() throws IOException, VersionNotFoundException {
        loadFakeVersions(200, true);
        HockeyAppVersion version = client.findVersion(VERSION_SHORT_1_0);
        assertEquals(VERSION_SHORT_1_0, version.getShortVersion());
    }

    @Test(expected = VersionNotFoundException.class)
    public void invalidFindVersion() throws IOException, VersionNotFoundException {
        loadFakeVersions(200, true);
        client.findVersion(null);
    }

    @Test
    public void getVersionCreate() throws IOException {
        String shortVersion = "1.6";
        buildTestVersions();
        stubCreatedVersionResponse(201, shortVersion);

        requestBuilder.setAppVersion(shortVersion);
        client.setVersion(null);

        assertEquals(client.getVersion().getShortVersion(), shortVersion);
    }

    @Test
    public void getVersionsWithNull() throws IOException {
        client.setVersions(null);
        loadFakeVersions(200);
        client.getVersions();
    }

    @Test
    public void createNewVersion() throws IOException {
        String shortVersion = "1.5";
        buildTestVersions();
        stubCreatedVersionResponse(201, shortVersion);

        HockeyAppVersion version = client.createVersion(shortVersion);
        client.setVersion(version);

        assertEquals(shortVersion, client.getVersion().getShortVersion());
    }

    @Test(expected = IOException.class)
    public void invalidCreateResponse() throws IOException {
        stubCreatedVersionResponse(500, VERSION_SHORT_1_0);
        client.createVersion(VERSION_SHORT_1_0);
    }

    @Test(expected = IOException.class)
    public void parseInvalidVersionsResponse() throws IOException {
        String invalidJsonResponse = "---";
        client.parseVersionsResponse(invalidJsonResponse);
    }

    @Test
    public void loadVersions() throws IOException, JSONException {
        loadFakeVersions(200, true);
        List<HockeyAppVersion> versions = client.getVersions();
        HockeyAppVersion version = versions.get(0);

        assertEquals(2, versions.size());
        assertEquals(VERSION_ID_1_0, version.getVersionId());
        assertEquals(VERSION_SHORT_1_0, version.getShortVersion());
    }

    private void buildTestVersions() throws IOException {
        List<HockeyAppVersion> versions = client.getVersions();
        versions.add(new HockeyAppVersion(VERSION_ID_1_0, VERSION_SHORT_1_0));
        versions.add(new HockeyAppVersion(VERSION_ID_1_1, VERSION_SHORT_1_1));
        client.setVersions(versions);
    }

    private void stubCreatedVersionResponse(int statusCode, String shortVersion) throws IOException {
        String responseContent = getVersionCreateResponseString(shortVersion);

        CloseableHttpResponseStub response = TestUtils.createResponseFromString(statusCode, responseContent);
        String newVersionUri = requestBuilder.buildApiUri("/app_versions/new");
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

    private void loadFakeVersions(int statusCode) throws IOException {
        stubVersionsResponse(statusCode);
    }

    private void stubVersionsResponse(int statusCode) {
        String versionsResponseContent = getVersionsJsonString();
        String versionsUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        CloseableHttpResponseStub response = TestUtils.createResponseFromString(statusCode, versionsResponseContent);
        httpClient.addResponse("GET", versionsUri, response);
    }

    private void loadFakeVersions(int statusCode, boolean loadClientVersion) throws IOException {
        stubVersionsResponse(statusCode);
        if (loadClientVersion) {
            client.loadVersions();
        }
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
}
