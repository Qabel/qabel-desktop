package de.qabel.desktop.update;

public interface UpdateChecker {
    VersionInformation loadInfos();

    boolean isCurrent(String version);

    LatestVersionInfo getDesktopVersion();

    boolean isAllowed(String version);
}
