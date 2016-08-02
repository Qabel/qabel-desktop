package de.qabel.desktop;

import de.qabel.desktop.config.LaunchConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class LaunchConfigurationReader {
    private InputStream propertyStream;
    private final Properties properties;

    public LaunchConfigurationReader(InputStream propertyStream) {
        this.propertyStream = propertyStream;
        properties = new Properties();
    }

    public LaunchConfig load() {
        try {
            properties.load(propertyStream);
            String dropUrl = get("drop.url");
            String accountingUrl = get("accounting.url");
            String blockUrl = get("block.url");
            String crashReportUrl = get("crashReport.url");
            return new LaunchConfig(
                new URL(dropUrl),
                new URL(accountingUrl),
                new URL(blockUrl),
                new URL(crashReportUrl));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("invalid configuration: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read properties: " + e.getMessage(), e);
        }
    }

    private String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("key not found in property file: '" + key + "'");
        }
        return value;
    }
}
