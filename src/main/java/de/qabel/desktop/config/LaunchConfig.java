package de.qabel.desktop.config;

import java.net.URL;

public class LaunchConfig {
    private URL dropUrl;

    public LaunchConfig(URL dropUrl) {
        this.dropUrl = dropUrl;
    }

    public URL getDropUrl() {
        return dropUrl;
    }
}
