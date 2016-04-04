package de.qabel.desktop.update;

public class VersionInformation {
    private AppInfos appinfos;

    public VersionInformation(AppInfos appinfos) {
        this.appinfos = appinfos;
    }

    public AppInfos getAppinfos() {
        return appinfos;
    }
}
