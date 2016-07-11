package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VersionClient {

    private final HockeyAppConfiguration config;
    //    private final String appId;
//    private final String securityTokenHeaderName;
//    private final String baseUri;
//    private final String securityToken;
    public String appVersion;
    public HttpClient httpClient;

    private List<HockeyAppVersion> versions = new LinkedList<>();
    private HockeyAppVersion version;

    public VersionClient(HockeyAppConfiguration config) {
        this.httpClient = config.getHttpClient();
        this.config = config;
//        this.appVersion = config.getAppVersion();
//        this.baseUri = config.getBaseUri();
//        this.securityToken = config.getSecurityToken();
//        this.securityTokenHeaderName = config.getTokenHeaderName();
//        this.appId = config.getApiAppKey();
    }

    String buildApiUri(String apiCallPath) {
        return config.getApiBaseUri() + apiCallPath;
    }

    List<HockeyAppVersion> getVersions() {
        return versions;
    }

    void setVersions(List<HockeyAppVersion> versions) {
        this.versions = versions;
    }

    void loadVersions() throws IOException, JSONException {

        HttpGet httpGet = new HttpGet(buildApiUri("/app_versions"));
        httpGet.addHeader(config.getSecurityTokenName(), config.getSecurityTokenKey());
        HttpResponse response = httpClient.execute(httpGet);
        String responseContent = EntityUtils.toString(response.getEntity());

        parseVersionsResponse(responseContent);
    }

    void findAndLoadVersion(String shortVersion) throws VersionNotFoundException {
        getVersions().forEach(version -> {
            if (shortVersion.equals(version.getShortVersion())) {
                setVersion(version);
            }
        });
        if (getVersion() == null) {
            throw new VersionNotFoundException("No Version with the shortversion: '" + shortVersion + "' found in HockeyApp");
        }
    }

    @Nullable
    HockeyAppVersion getVersion() {
        return version;
    }

    void setVersion(HockeyAppVersion version) {
        this.version = version;
    }

    void createVersion(String version) throws IOException, JSONException {
        HttpPost request = new HttpPost(buildApiUri("/app_versions/new"));
        request.addHeader(config.getSecurityTokenName(), config.getApiAppKey());

        List<NameValuePair> parameters = buildCreateParameters(version);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        HttpResponse response = httpClient.execute(request);
        String responseContent = EntityUtils.toString(response.getEntity());

        parseVersionCreateResponse(responseContent);
    }

    List<HockeyAppVersion> parseVersionsResponse(String responseContent) throws IOException {
        versions.clear();
        try {
            JSONObject parsedJson = new JSONObject(responseContent);
            JSONArray versionArray = parsedJson.getJSONArray("app_versions");
            for (int i = 0; i < versionArray.length(); i++) {
                JSONObject jsonObj = versionArray.getJSONObject(i);
                int versionId = jsonObj.getInt("version");
                String shortVersion = jsonObj.getString("shortversion");
                versions.add(new HockeyAppVersion(versionId, shortVersion));
            }
            return versions;
        } catch (JSONException e) {
            throw new IOException("returned JSON was invalid", e);
        }
    }

    void parseVersionCreateResponse(String responseContent) throws IOException {
        try {
            JSONObject parsedJson = new JSONObject(responseContent);
            int versionId = parsedJson.getInt("id");
            String shortVersion = parsedJson.getString("shortversion");
            setVersion(new HockeyAppVersion(versionId, shortVersion));
        } catch (JSONException e) {
            throw new IOException("returned JSON was invalid", e);
        }
    }

    List<NameValuePair> buildCreateParameters(String version) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("bundle_short_version", version));
        return parameters;
    }
}
