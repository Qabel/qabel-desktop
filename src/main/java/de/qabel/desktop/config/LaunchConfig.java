package de.qabel.desktop.config;

import java.net.URL;

public class LaunchConfig {

    private URL dropUrl;
    private URL accountingUrl;
    private URL blockUrl;

    public LaunchConfig(URL dropUrl, URL accountingUrl, URL blockUrl) {
        this.dropUrl = dropUrl;
        this.accountingUrl = accountingUrl;
        this.blockUrl = blockUrl;
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
}
