package de.qabel.desktop.hockeyapp;

public class HockeyAppVersion {

    private String versionId;
    private String shortVersion;

    public HockeyAppVersion(String versionId, String shortVersion) {
        this.versionId = versionId;
        this.shortVersion = shortVersion;
    }

    public HockeyAppVersion(String versionId) {
        this.versionId = versionId;
        this.shortVersion = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getShortVersion() {
        return shortVersion;
    }
}
