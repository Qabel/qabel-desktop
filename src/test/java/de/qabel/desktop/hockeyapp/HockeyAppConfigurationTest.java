package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpClientStub;
import org.junit.Ignore;

@Ignore
public class HockeyAppConfigurationTest {

    CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
    HockeyAppConfiguration client = new HockeyAppConfiguration("1.1", httpClientStub);

    VersionClient versionClient = new VersionClient(client);


}
