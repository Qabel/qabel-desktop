package de.qabel.desktop.hockeyapp;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VersionClient {

    private static final String API_VERSIONS_NEW = "/app_versions/new";
    private static final String API_VERSIONS_ALL = "/app_versions";

    private final HockeyAppRequestBuilder requestBuilder;

    private List<HockeyAppVersion> versions;
    private HockeyAppVersion version;

    public VersionClient(HockeyAppRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    List<HockeyAppVersion> loadVersions() throws IOException, JSONException {
        HttpGet httpGet = requestBuilder.getHttpGet(API_VERSIONS_ALL);
        HttpResponse response = requestBuilder.getHttpClient().execute(httpGet);
        String responseContent = EntityUtils.toString(response.getEntity());

        return parseVersionsResponse(responseContent);
    }

    HockeyAppVersion createVersion(String version) throws IOException, JSONException {
        HttpPost request = requestBuilder.getHttpPost(API_VERSIONS_NEW);

        List<NameValuePair> parameters = buildCreateParameters(version);
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        HttpResponse response = requestBuilder.getHttpClient().execute(request);
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new IOException("Create version(" + API_VERSIONS_NEW + ") failed! Wrong status code");
        }
        String responseContent = EntityUtils.toString(response.getEntity());

        return parseVersionCreateResponse(responseContent);
    }

    List<HockeyAppVersion> parseVersionsResponse(String responseContent) throws IOException {
        try {
            versions = new LinkedList<>();
            JSONObject parsedJson = new JSONObject(responseContent);
            JSONArray versionArray = parsedJson.getJSONArray("app_versions");
            for (int i = 0; i < versionArray.length(); i++) {
                JSONObject jsonObj = versionArray.getJSONObject(i);

//                String versionId = jsonObj.getString("id");
                String shortVersion = jsonObj.getString("version");

                versions.add(new HockeyAppVersion(shortVersion));
            }
            return versions;
        } catch (JSONException e) {
            throw new IOException("returned JSON was invalid", e);
        }
    }

    HockeyAppVersion parseVersionCreateResponse(String responseContent) throws IOException {
        try {
            JSONObject parsedJson = new JSONObject(responseContent);
            String versionId = parsedJson.getString("id");
            String shortVersion = parsedJson.getString("version");

            return new HockeyAppVersion(versionId, shortVersion);
        } catch (JSONException e) {
            throw new IOException("returned JSON was invalid", e);
        }
    }

    List<NameValuePair> buildCreateParameters(String version) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("bundle_version", version));
        parameters.add(new BasicNameValuePair("bundle_short_version", version));
        return parameters;
    }

    HockeyAppVersion findVersion(String shortVersion) throws VersionNotFoundException, IOException {
        if (shortVersion == null || shortVersion.isEmpty()) {
            throw new VersionNotFoundException("Version: " + shortVersion + " not found!");
        }
        if (versions == null) {
            versions = getVersions();
        }
        for (HockeyAppVersion version : versions) {
            if (shortVersion.equals(version.getShortVersion())) {
                return version;
            }
        }
        return version;
    }

    public HockeyAppVersion getVersion() throws IOException {
        if (version == null) {
            try {
                version = findVersion(requestBuilder.getAppVersion());
            } catch (VersionNotFoundException e) {
                version = createVersion(requestBuilder.getAppVersion());
            }
        }
        return version;
//        throw new IOException("something went wrong, findVersion or createVersion returned wrong");
    }

    List<HockeyAppVersion> getVersions() throws IOException {
        if (versions == null) {
            return versions = loadVersions();
        }
        return versions;
    }

    void setVersion(HockeyAppVersion version) {
        this.version = version;
    }


    void setVersions(List<HockeyAppVersion> versions) {
        this.versions = versions;
    }
}
