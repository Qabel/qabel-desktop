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
import static org.junit.Assert.assertNull;

public class VersionClientTest {

    private static final int VERSION_ID_1_1 = 208;
    private static final int VERSION_ID_1_0 = 195;
    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    private HockeyAppClient hockeyAppClient = new HockeyAppClient("1.1", httpClientStub);
    private VersionClient client = new VersionClient(hockeyAppClient);

    @Test
    public void checkAppVersion() {
        assertEquals("1.1", client.appVersion);
    }

    @Test
    public void findVersion() throws VersionNotFoundException {

        String shortVersion = "1.1";

        buildTestVersions();

        client.findAndLoadVersion(shortVersion);
        assertEquals(shortVersion, client.getVersion().getShortVersion());


    }

    @Test(expected = VersionNotFoundException.class)
    public void noVersion() throws VersionNotFoundException {
        String shortVersion = "noversion";
        client.setVersion(null);

        assertNull(client.getVersion());
        if (client.getVersion() == null) {
            throw new VersionNotFoundException("No Version with the shortversion: '" + shortVersion + "' found in HockeyApp");
        }
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
    public void parseVersionCreateResponse() throws IOException {
        String versionsResponseContent = getVersionCreateResponseString();
        client.parseVersionCreateResponse(versionsResponseContent);

        assertEquals(VERSION_ID_1_1, client.getVersion().getVersionId());
    }

    @Test
    public void parseVersionsResponse() throws IOException {
        String versionsResponseContent = getVersionsJsonString();
        List<HockeyAppVersion> versions = client.parseVersionsResponse(versionsResponseContent);
        assertEquals(2, versions.size());
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
        assertEquals(VERSION_ID_1_1, client.getVersion().getVersionId());
    }

    public void createVersion(String version) throws IOException {
        loadFakeCreatedVersionResponse();
        client.createVersion(version);
    }


    @Test
    public void loadVersions() throws IOException, JSONException {
        loadFakeVersions();

        List<HockeyAppVersion> versions = client.getVersions();


        assertEquals(2, versions.size());

        assertEquals(VERSION_ID_1_1, versions.get(0).getVersionId());
        assertEquals(VERSION_ID_1_0, versions.get(1).getVersionId());

        assertEquals("1.1", versions.get(0).getShortVersion());
        assertEquals("1.0", versions.get(1).getShortVersion());
    }

    @Test
    public void buildApiUri() {
        String testUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/somewhere";
        assertEquals(testUri, client.buildApiUri("/somewhere"));
    }


    private void buildTestVersions() {
        List<HockeyAppVersion> versions = client.getVersions();
        versions.add(new HockeyAppVersion(VERSION_ID_1_1, "1.1"));
        versions.add(new HockeyAppVersion(VERSION_ID_1_0, "1.0"));
        client.setVersions(versions);
    }

    private void loadFakeCreatedVersionResponse() throws IOException {
        String responseContent = getVersionCreateResponseString();

        CloseableHttpResponseStub response = this.createResponseFromString(201, responseContent);
        String newVersionUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions/new";
        httpClientStub.addResponse("POST", newVersionUri, response);
    }

    @NotNull
    private String getVersionCreateResponseString() {
        return "{\n" +
            "    \"id\": \"" + VERSION_ID_1_1 + "\",\n" +
            "    \"shortversion\": \"1.1\",\n" +
            "    \"title\": \"createNewVersion\",\n" +
            "    \"timestamp\": 1467877960,\n" +
            "    \"version\": \"23\",\n" +
            "}";
    }

    @Test
    public void buildParameters() {

        List<NameValuePair> params = client.buildCreateParameters("1.1");
        String keyName = params.get(0).getName();
        String value = params.get(0).getValue();

        assertEquals("bundle_short_version", keyName);
        assertEquals("1.1", value);

    }

    private void loadFakeVersions() throws IOException {
        String versionsResponseContent = getVersionsJsonString();

        String versionsUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        CloseableHttpResponseStub response = this.createResponseFromString(200, versionsResponseContent);
        httpClientStub.addResponse("GET", versionsUri, response);
        client.loadVersions();
    }

    @NotNull
    private String getVersionsJsonString() {
        return "{\n" +
            "    \"app_versions\": [\n" +
            "        {\n" +
            "            \"version\": \"" + VERSION_ID_1_1 + "\",\n" +
            "            \"shortversion\": \"1.1\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/" + VERSION_ID_1_1 + "\",\n" +
            "            \"download_url\":\"https://rink.hockeyapp.net/apps/0873e2b98ad046a92c170a243a8515f6/app_versions/" + VERSION_ID_1_1 + "\",\n" +
            "            \"timestamp\": 1326195742,\n" +
            "            \"appsize\": 157547,\n" +
            "            \"device_family\": null,\n" +
            "            \"notes\": \"<p>Fixed bug when users could not sign in.</p>\\n\",\n" +
            "            \"status\": 2,\n" +
            "            \"minimum_os_version\": null,\n" +
            "            \"title\": \"HockeyApp\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"version\": \"" + VERSION_ID_1_0 + "\",\n" +
            "            \"shortversion\": \"1.0\",\n" +
            "            \"mandatory\": false,\n" +
            "            \"config_url\": \"https://rink.hockeyapp.net/manage/apps/1266/app_versions/" + VERSION_ID_1_0 + "\",\n" +
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
