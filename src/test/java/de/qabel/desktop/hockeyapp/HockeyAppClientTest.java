package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HockeyAppClientTest {

    public static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    public static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    public static final String TOKEN = "350b097ef0964b17a0f3907050de309d";

    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();

    String fakeCurrentClientVersion = "1.1";
    private HockeyAppClient client = new HockeyAppClient(fakeCurrentClientVersion, httpClientStub);

    @Before
    public void setUp(){

    }


    @Test
    public void versionExists() throws IOException, VersionNotFoundException {

        client.versions = new LinkedList<>();
        client.versions.add(new HockeyAppVersion(1,"1.1"));
        client.versions.add(new HockeyAppVersion(2, "2.0"));

        HockeyAppVersion version = client.getVersion(fakeCurrentClientVersion);

        assertEquals(fakeCurrentClientVersion, version.getShortVersion());
    }

    @Test
    public void versionNotExists() throws IOException {

        client.versions = new LinkedList<>();
        client.versions.add(new HockeyAppVersion(1,"1.2"));
        client.versions.add(new HockeyAppVersion(2, "2.0"));

        try {
            HockeyAppVersion version = client.getVersion(fakeCurrentClientVersion);
        } catch (VersionNotFoundException ignored) {
            HockeyAppVersion newVersion = createVersion(fakeCurrentClientVersion);
            assertEquals(fakeCurrentClientVersion, newVersion.getShortVersion());
        }

    }


    public HockeyAppVersion createVersion(String fakeCurrentClientVersion) throws IOException {

        String responseContent  = "{\n" +
            "    \"title\": \"createVersion\",\n" +
            "    \"timestamp\": 1467877960,\n" +
            "    \"id\": \"1337\",\n" +
            "    \"version\": \"23\",\n" +
            "    \"shortversion\": \"1.1\",\n" +
            "}";

        CloseableHttpResponseStub response = this.createResponseFromString(201, responseContent);
        String newVersionUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions/new";
        httpClientStub.addResponse("POST", newVersionUri, response);

        HockeyAppVersion newVersion = client.createNewVersion(fakeCurrentClientVersion);
        return newVersion;
    }


    @Test
    public void currentAppVersion(){
        assertEquals(fakeCurrentClientVersion, client.currentClientVersion);
    }

    @Test
    public void buildApiUri(){
        String testUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/somewhere";
        assertEquals(testUri, client.buildApiUri("/somewhere"));
    }

    @Test
    public void versionList() throws IOException {

        String responseContent = "{\n" +
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

        List<HockeyAppVersion> versions = getVersions(responseContent);

        assertEquals(2, versions.size());

        assertEquals(208, versions.get(0).getVersionId());
        assertEquals(195, versions.get(1).getVersionId());

        assertEquals("1.1", versions.get(0).getShortVersion());
        assertEquals("1.0", versions.get(1).getShortVersion());
    }

    private List<HockeyAppVersion> getVersions(String responseContent) throws IOException {
        CloseableHttpResponseStub response = this.createResponseFromString(200, responseContent);
        String versionsUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        httpClientStub.addResponse("GET", versionsUri, response);
        return client.getVersions();
    }

    @Test
    public void getVersion() throws IOException {

        String responseContent = "{\n" +
            "    \"app_versions\": [\n" +
            "        {\n" +
            "            \"version\": \"211\",\n" +
            "            \"shortversion\": \"2.0\",\n" +
            "        },\n" +
            "\n" +
            "    ],\n" +
            "    \"status\": \"success\"\n" +
            "}";

        List<HockeyAppVersion> versions = getVersions(responseContent);

        assertEquals(1, versions.size());
        assertEquals(211, versions.get(0).getVersionId());
        assertEquals("2.0", versions.get(0).getShortVersion());
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
