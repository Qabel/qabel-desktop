package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.core.accounting.CloseableHttpResponseStub;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.crashReports.HockeyApp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.amazonaws.services.simpleworkflow.flow.junit.AsyncAssert.assertEquals;
import static org.spongycastle.crypto.tls.ConnectionEnd.client;

public class VersionClient {

    private final String appId;
    public String appVersion;

    private final String baseUri;
    private final String securityToken;
    public HttpClient httpClient;

    private List<HockeyAppVersion> versions = new LinkedList<HockeyAppVersion>();
    private HockeyAppVersion version;

    public VersionClient(HockeyAppClient hockeyAppClient) {
        this.httpClient = hockeyAppClient.getHttpClient();
        this.appVersion = hockeyAppClient.getAppVersion();
        this.baseUri = hockeyAppClient.getBaseUri();
        this.securityToken = hockeyAppClient.getSecurityToken();
        this.appId = hockeyAppClient.getAppId();
    }

    String buildApiUri(String apiCallPath){
        return baseUri + apiCallPath;
    }


    public List<HockeyAppVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<HockeyAppVersion> versions) {
        this.versions = versions;
    }

    void loadVersions() throws IOException {
        List<HockeyAppVersion> versions = new LinkedList<>();

        HttpGet httpGet = new HttpGet(buildApiUri("/app_versions"));
        httpGet.addHeader("X-HockeyAppToken", securityToken);
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
        setVersions(versions);
    }

    /**
     * Sets version by given "shortVersion"
     * @param shortVersion (ex.: "1.1")
     */
    void findAndLoadVersion(String shortVersion) throws VersionNotFoundException {
        getVersions().forEach(version -> {
            if(shortVersion.equals(version.getShortVersion())){
                setVersion(version);
            }
        });
        if(getVersion() == null){
            throw new VersionNotFoundException("No Version with the shortversion: '" + shortVersion + "' found in HockeyApp");
        }

    }

    public void setVersion(HockeyAppVersion version) {
        this.version = version;
    }

    public HockeyAppVersion getVersion() {
        return version;
    }

    public void createVersion(String version) throws IOException {
        HttpPost request = new HttpPost(buildApiUri("/app_versions/new"));
        request.addHeader("X-HockeyAppToken", this.appId);

        //build parameters
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("bundle_short_version", version));
        request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        HttpResponse response = httpClient.execute(request);
        String responseContent = EntityUtils.toString(response.getEntity());

        try {
            JSONObject parsedJson = new JSONObject(responseContent);
            int versionId = parsedJson.getInt("id");
            String shortVersion = parsedJson.getString("shortversion");
            setVersion(new HockeyAppVersion(versionId, shortVersion));
        } catch (JSONException e){
            throw new IOException("Unexpected JSON format return from HokkeyApp", e);
        }
    }
}
