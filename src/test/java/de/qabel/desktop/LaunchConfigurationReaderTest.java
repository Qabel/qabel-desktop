package de.qabel.desktop;

import de.qabel.desktop.config.LaunchConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class LaunchConfigurationReaderTest {
    @Test
    public void loadsDropUrls() throws Exception {
        String properties = "drop.url=http://localhost:5000\n" +
            "accounting.url=http://localhost:9696\n" +
            "block.url=http://localhost:9697\n" +
            "index.url=http://localhost:9698\n" +
            "crashReport.url=https://rink.hockeyapp.net/api/2/apps/";
        InputStream in = new ByteArrayInputStream(properties.getBytes());
        LaunchConfigurationReader reader = new LaunchConfigurationReader(in);
        LaunchConfig config = reader.load();

        assertEquals("http://localhost:5000", config.getDropUrl().toString());
        assertEquals("http://localhost:9696", config.getAccountingUrl().toString());
        assertEquals("http://localhost:9697", config.getBlockUrl().toString());
        assertEquals("http://localhost:9698", config.getIndexUrl().toString());

        assertEquals("https://rink.hockeyapp.net/api/2/apps/", config.getCrashReportUrl().toString());
    }
}
