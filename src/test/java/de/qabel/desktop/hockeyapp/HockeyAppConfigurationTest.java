package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HockeyAppConfigurationTest {

    private CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    private HockeyAppConfiguration config = new HockeyAppConfiguration("1.1", httpClientStub);

    private VersionClient versionClient = new VersionClient(config, httpClientStub);

    @Test
    public void validUri() throws IllegalStateException {
        String expectedUri = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        String path = "/app_versions";
        String uri = config.buildApiUri(path);

        assertEquals(expectedUri, uri);
    }

}
