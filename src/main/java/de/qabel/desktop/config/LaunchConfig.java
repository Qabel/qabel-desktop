package de.qabel.desktop.config;

import java.net.URL;

public class LaunchConfig {

    private URL dropUrl;
    private URL accountingUrl;
    private URL blockUrl;
    private URL crashReportUrl;

    public LaunchConfig(URL dropUrl, URL accountingUrl, URL blockUrl, URL crashReportUrl) {
        this.dropUrl = dropUrl;
        this.accountingUrl = accountingUrl;
        this.blockUrl = blockUrl;
        this.crashReportUrl = crashReportUrl;
    }

    public URL getDropUrl() {
        return dropUrl;
    }

    public URL getAccountingUrl() {
        return accountingUrl;
    }

    public URL getBlockUrl() {
        return blockUrl;
    }

    public URL getCrashReportUrl() {
        return crashReportUrl;
    }
}
