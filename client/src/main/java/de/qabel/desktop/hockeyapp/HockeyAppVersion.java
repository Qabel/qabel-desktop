package de.qabel.desktop.hockeyapp;

public class HockeyAppVersion {

    public String versionId;
    public String shortVersion;

    public HockeyAppVersion(String versionId, String shortVersion) {
        this.versionId = versionId;
        this.shortVersion = shortVersion;
    }

    public HockeyAppVersion(String versionId) {
        this.versionId = versionId;
        this.shortVersion = versionId;
    }

}
