package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import de.qabel.desktop.LaunchConfigurationReader;
import de.qabel.desktop.config.LaunchConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HockeyAppRequestBuilderTest {

    private CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();

    private HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder("uri/", "1.1", httpClientStub);

    private VersionClient versionClient = new VersionClient(requestBuilder);

    @Test
    public void validUri() throws IllegalStateException {
        String expectedUri = "uri/3b119dc227334d2d924e4e134c72aadc/app_versions";
        String path = "/app_versions";
        String uri = requestBuilder.buildApiUri(path);

        assertEquals(expectedUri, uri);
    }

}
