package de.qabel.desktop.update;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


public class HttpUpdateChecker implements UpdateChecker {
    private final HttpClient client;
    private LatestVersionInfo desktopVersion;

    public HttpUpdateChecker(HttpClient client) {
        this.client = client;
    }

    public HttpUpdateChecker() {
        this(HttpClientBuilder.create().build());
    }

    @Override
    public VersionInformation loadInfos() {
        HttpGet request = new HttpGet("https://files.qabel.de/etc/versions.json");
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            String body = IOUtils.toString(response.getEntity().getContent());
            return new Gson().fromJson(body, VersionInformation.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check for updates: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isCurrent(String version) {
        version = trim(version);
        load();
        Version currentVersion = Version.valueOf(desktopVersion.getCurrentAppVersion());
        Version appVersion = Version.valueOf(version);
        return appVersion.greaterThanOrEqualTo(currentVersion);
    }

    private String trim(String version) {
        return version.replace("\n", "");
    }

    private void load() {
        if (desktopVersion == null) {
            desktopVersion = loadInfos().getAppinfos().getDesktop();
        }
    }

    @Override
    public LatestVersionInfo getDesktopVersion() {
        load();
        return desktopVersion;
    }

    @Override
    public boolean isAllowed(String version) {
        version = trim(version);
        load();
        Version minimumVersion = Version.valueOf(desktopVersion.getMinimumAppVersion());
        Version appVersion = Version.valueOf(version);
        return appVersion.greaterThanOrEqualTo(minimumVersion);
    }
}
