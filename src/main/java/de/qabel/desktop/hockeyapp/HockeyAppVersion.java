package de.qabel.desktop.hockeyapp;

public class HockeyAppVersion {

    private int versionId;
    private String shortVersion;

    public HockeyAppVersion(int versionId, String shortVersion) {
        this.versionId = versionId;
        this.shortVersion = shortVersion;
    }


    public int getVersionId() {
        return versionId;
    }

    public String getShortVersion() {
        return this.shortVersion;
    }
}
