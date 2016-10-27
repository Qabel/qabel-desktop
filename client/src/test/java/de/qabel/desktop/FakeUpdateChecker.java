package de.qabel.desktop;

import de.qabel.desktop.update.AppInfos;
import de.qabel.desktop.update.HttpUpdateChecker;
import de.qabel.desktop.update.LatestVersionInfo;
import de.qabel.desktop.update.VersionInformation;

public class FakeUpdateChecker extends HttpUpdateChecker {
    private String currentVersion;
    private String minimumVersion;
    private String downloadUrl;

    public FakeUpdateChecker(String currentVersion, String minimumVersion, String downloadUrl) {
        this.currentVersion = currentVersion;
        this.minimumVersion = minimumVersion;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public VersionInformation loadInfos() {
        LatestVersionInfo desktop = new LatestVersionInfo(currentVersion,minimumVersion, downloadUrl);
        LatestVersionInfo android = new LatestVersionInfo("0.7.0", "0.6.0", "http://apk");
        return new VersionInformation(new AppInfos(
            desktop,
            android
        ));
    }

    public static FakeUpdateCheckerBuilder builder() {
        return new FakeUpdateCheckerBuilder();
    }

    public static class FakeUpdateCheckerBuilder {
        private String currentVersion = "1.0.0";
        private String minimumVersion = "0.1.0";
        private String downloadUrl = "http://download";

        public FakeUpdateCheckerBuilder current(String version) {
            currentVersion = version;
            return this;
        }

        public FakeUpdateCheckerBuilder minimum(String version) {
            minimumVersion = version;
            return this;
        }

        public FakeUpdateCheckerBuilder url(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public FakeUpdateChecker build() {
            return new FakeUpdateChecker(currentVersion, minimumVersion, downloadUrl);
        }
    }
}
