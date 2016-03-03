package de.qabel.desktop.update;

public class AppInfos {
	private LatestVersionInfo desktop;
	private LatestVersionInfo android;

	public AppInfos(LatestVersionInfo desktop, LatestVersionInfo android) {
		this.desktop = desktop;
		this.android = android;
	}

	public LatestVersionInfo getDesktop() {
		return desktop;
	}

	public LatestVersionInfo getAndroid() {
		return android;
	}
}
