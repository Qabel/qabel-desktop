package de.qabel.desktop.update;

public interface UpdateChecker {
    abstract VersionInformation loadInfos();

    boolean isCurrent(String version);

    LatestVersionInfo getDesktopVersion();

    boolean isAllowed(String version);
}
