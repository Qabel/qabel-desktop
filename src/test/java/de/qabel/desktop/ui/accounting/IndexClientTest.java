package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.core.index.IndexHTTP;
import de.qabel.core.index.IndexHTTPLocation;
import de.qabel.core.index.UpdateResult;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class IndexClientTest extends AbstractControllerTest {

    private static final String TEST_ALIAS = "IndexClient TestAlias";
    private Identity indenty;

    private IndexHTTP indexHTTP = new IndexHTTP(new IndexHTTPLocation("http://localhost:9698"), HttpClients.createMinimal());
    private IndexClient client = new IndexClient(indexHTTP);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        identity = identityBuilderFactory.factory()
            .withAlias(TEST_ALIAS)
            .build();
        identity.setEmail("foo@bar.com");
        identity.setPhone("0123123123123");
    }

    @Test
    public void canUpdateIdentity() {
        UpdateResult result = client.updateIdentity(identity);
        assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, result);
    }
}
