package de.qabel.desktop.update;

public class LatestVersionInfo {
	private String currentAppVersion;
	private String minimumAppVersion;
	private String downloadURL;

	public LatestVersionInfo(String currentAppVersion, String minimumAppVersion, String downloadURL) {
		this.currentAppVersion = currentAppVersion;
		this.minimumAppVersion = minimumAppVersion;
		this.downloadURL = downloadURL;
	}

	public String getCurrentAppVersion() {
		return currentAppVersion;
	}

	public String getMinimumAppVersion() {
		return minimumAppVersion;
	}

	public String getDownloadURL() {
		return downloadURL;
	}
}
