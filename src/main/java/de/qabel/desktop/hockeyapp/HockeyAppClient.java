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
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HockeyAppClient {

    private static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";
    public String currentClientVersion;
    public HockeyAppVersion currentHockeyVersion;
    HttpClient httpClient;
    List<HockeyAppVersion> versions;

    public HockeyAppClient(String currentClientVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.currentClientVersion = currentClientVersion;
    }

    String buildApiUri(String apiCallPath){
        return BASE_URI + APP_ID + apiCallPath;
    }

    public List<HockeyAppVersion> getVersions() throws IOException {
        if(versions == null){
            versions = fetchVersions();
        }
        return versions;
    }

    @NotNull
    private List<HockeyAppVersion> fetchVersions() throws IOException {
        List<HockeyAppVersion> versions = new LinkedList<>();

        HttpGet httpGet = new HttpGet(buildApiUri("/app_versions"));
        httpGet.addHeader("X-HockeyAppToken", TOKEN);
        HttpResponse response = httpClient.execute(httpGet);
        String responseContent = EntityUtils.toString(response.getEntity());

        JSONObject parsedJson = new JSONObject(responseContent);
        JSONArray versionArray = parsedJson.getJSONArray("app_versions");

        for (int i=0; i < versionArray.length(); i++){
            JSONObject jsonObj = versionArray.getJSONObject(i);
            int versionId = jsonObj.getInt("version");
            String shortVersion = jsonObj.getString("shortversion");

            versions.add(new HockeyAppVersion(versionId, shortVersion));
        }
        return versions;
    }

    public HockeyAppVersion createNewVersion(String version) throws IOException {

        HttpPost request = new HttpPost(buildApiUri("/app_versions/new"));
        request.addHeader("X-HockeyAppToken", TOKEN);

        //build parameters
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("bundle_short_version", version));
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        HttpResponse response = httpClient.execute(request);
        String responseContent = EntityUtils.toString(response.getEntity());
        JSONObject parsedJson = new JSONObject(responseContent);

        if(currentHockeyVersion == null){
            currentHockeyVersion = new HockeyAppVersion(parsedJson.getInt("id"),parsedJson.getString("shortversion"));
        }
        return currentHockeyVersion;
    }

    public HockeyAppVersion getVersion(String shortVersion) throws VersionNotFoundException {
        for (HockeyAppVersion current : versions) {
            if (shortVersion.equals(current.getShortVersion())) {
                return current;
            }
        }
        throw new VersionNotFoundException("No Version with the shortversion: '"+shortVersion+"' found in HockeyApp");
    }
}
