package de.qabel.desktop;

import de.qabel.desktop.config.LaunchConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class LaunchConfigurationReaderTest {
    @Test
    public void loadsDropUrls() throws Exception {
        String properties = "drop.url=http://localhost:5000";
        InputStream in = new ByteArrayInputStream(properties.getBytes());
        LaunchConfigurationReader reader = new LaunchConfigurationReader(in);
        LaunchConfig config = reader.load();
        assertEquals("http://localhost:5000", config.getDropUrl().toString());
    }
}
