package de.qabel.desktop.hockeyapp;

import de.qabel.desktop.crashReports.CrashReportHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HockeyAppClient implements CrashReportHandler {

    private static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";
    private HttpClient httpClient;

    public List<HockeyAppVersion> versions;
    public String currentClientVersion;
    private HockeyAppVersion currentHockeyVersion;

    public HockeyAppClient(String currentClientVersion, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.currentClientVersion = currentClientVersion;
    }

    public void initVersion() {
        try {
            findOrCreateVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFeedback(String feedback, String name, String email) throws IOException {

        initVersion();

        HttpPost httpPost = new HttpPost(buildApiUri("/feedback"));
        httpPost.addHeader("X-HockeyAppToken", TOKEN);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("text", feedback));
        parameters.add(new BasicNameValuePair("name", name));
        parameters.add(new BasicNameValuePair("email", email));

        String appVersionID = Integer.toString(getCurrentHockeyVersion().getVersionId());
        parameters.add(new BasicNameValuePair("app_version_id", appVersionID));

        httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

        httpClient.execute(httpPost);
    }

    public void findOrCreateVersion() throws IOException {
        try {
            findVersion(currentClientVersion);
        } catch (VersionNotFoundException ignored) {
            createNewVersion(currentClientVersion);
        }
    }

    @Override
    public void sendStacktrace(String feedback, String stacktrace) throws IOException {

        String log = createLog(stacktrace);

        HttpPost httpPost = new HttpPost(buildApiUri("/crashes/upload"));

        HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("log", new ByteArrayBody(log.getBytes(), "log"))
            .addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
            .build();
        httpPost.setEntity(entity);

        httpClient.execute(httpPost);
    }

    private String createLog(String stacktrace) {
        Date date = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: de.qabel.desktop\n");
        log.append("Version: 1\n");
        log.append("OS: " + System.getProperty("os.name") + " / ");
        log.append(System.getProperty("os.arch") + " / ");
        log.append(System.getProperty("os.version") + "\n");
        log.append("Manufacturer: " + System.getProperty("java.vendor") + "\n");
        log.append("Model: " + System.getProperty("java.version") + "\n");
        log.append("Date: " + date + "\n");
        log.append("\n");
        log.append(stacktrace);

        return log.toString();
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


    public HockeyAppVersion getCurrentHockeyVersion() {
        return currentHockeyVersion;
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

        setCurrentHockeyVersion(new HockeyAppVersion(parsedJson.getInt("id"),parsedJson.getString("shortversion")));

        return currentHockeyVersion;
    }


    public HockeyAppVersion findVersion(String shortVersion) throws VersionNotFoundException {
        for (HockeyAppVersion current : versions) {
            if (shortVersion.equals(current.getShortVersion())) {
                this.setCurrentHockeyVersion(current);
                return current;
            }
        }
        throw new VersionNotFoundException("No Version with the shortversion: '" + shortVersion + "' found in HockeyApp");
    }

    public void setCurrentHockeyVersion(HockeyAppVersion currentHockeyVersion) {
        this.currentHockeyVersion = currentHockeyVersion;
    }
}
