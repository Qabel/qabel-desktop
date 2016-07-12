package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HockeyAppConfigurationTest {

    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    HockeyAppConfiguration config = new HockeyAppConfiguration("1.1", httpClientStub);

    VersionClient versionClient = new VersionClient(config, httpClientStub);

    @Test
    public void validUri() throws IllegalStateException {
        String uriStr = "https://rink.hockeyapp.net/api/2/apps/3b119dc227334d2d924e4e134c72aadc/app_versions";
        String path = "/app_versions";

        String uri = config.buildApiUri(path);

        assertEquals(uriStr, uri);
    }

}
