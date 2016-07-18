package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.NameValuePair;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VersionClientTest {

    private static final String VERSION_1_0 = "1.0";
    private static final String VERSION_1_1 = "1.1";

    private CloseableHttpClientStub httpClient = new CloseableHttpClientStub();
    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder(VERSION_1_0, httpClient);
    private VersionClient client = new VersionClient(requestBuilder);

    @Test
    public void validFindVersion() throws IOException, VersionNotFoundException {
        loadFakeVersions(200, true);
        HockeyAppVersion version = client.findVersion(VERSION_1_0);
        assertEquals(VERSION_1_0, version.shortVersion);
    }

    @Test(expected = VersionNotFoundException.class)
    public void invalidFindVersion() throws IOException, VersionNotFoundException {
        loadFakeVersions(200, true);
        client.findVersion(null);
    }

    @Test
    public void getVersions() throws VersionNotFoundException, IOException {
        LinkedList<HockeyAppVersion> versions = new LinkedList<>();
        versions.add(new HockeyAppVersion(requestBuilder.getAppVersion()));
        versions.add(new HockeyAppVersion("1.2"));
        client.setVersions(versions);
        stubCreatedVersionResponse(201, requestBuilder.getAppVersion());
        client.getVersion();
    }

    @Test
    public void getVersionsWithNull() throws IOException {
        client.setVersions(null);
        stubVersionsResponse(200);
        client.getVersions();
    }

    @Test
    public void createNewVersion() throws IOException {
        String shortVersion = "1.5";
        buildTestVersions();
        stubCreatedVersionResponse(201, shortVersion);

        HockeyAppVersion version = client.createVersion(shortVersion);
        client.setVersion(version);

        assertEquals(shortVersion, version.shortVersion);
    }

    @Test(expected = IOException.class)
    public void invalidCreateResponse() throws IOException {
        stubCreatedVersionResponse(500, VERSION_1_0);
        client.createVersion(VERSION_1_0);
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
        assertEquals(VERSION_1_0, version.shortVersion);
        assertEquals(VERSION_1_0, version.shortVersion);
    }

    private void buildTestVersions() throws IOException {
        List<HockeyAppVersion> versions = new LinkedList<>();
        versions.add(new HockeyAppVersion(VERSION_1_0));
        versions.add(new HockeyAppVersion(VERSION_1_1));
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
            "    \"id\": \"" + shortVersion + "\",\n" +
            "    \"version\": \"" + shortVersion + "\",\n" +
            "    \"shortversion\": \"" + shortVersion + "\",\n" +
            "    \"title\": \"createNewVersion\",\n" +
            "    \"timestamp\": 1467877960,\n" +

            "}";
    }

    @Test
    public void buildParameters() throws Exception {
        List<NameValuePair> params = client.buildCreateParameters(VERSION_1_1);
        assertEquals(VERSION_1_1, TestUtils.getValueByKey(params, "bundle_version"));
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
            "            \"version\": \"" + VERSION_1_0 + "\",\n" +
            "            \"shortversion\": \"" + VERSION_1_0 + "\",\n" +

            "        },\n" +
            "        {\n" +
            "            \"version\": \"" + VERSION_1_1 + "\",\n" +
            "            \"shortversion\": \"" + VERSION_1_1 + "\",\n" +

            "        },\n" +
            "\n" +
            "    ],\n" +
            "    \"status\": \"success\"\n" +
            "}";
    }
}
