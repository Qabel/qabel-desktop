package de.qabel.desktop.ui.connector;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.*;
import de.qabel.core.http.DropHTTP;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HttpDropConnectorTest extends AbstractControllerTest {

    Identity i;
    Contact c;
    String fakeURL;
    String workingURL;
    String text = "MessageString";
    HttpDropConnector connector;
    private NetworkStatus networkStatus;
    private DropHTTP dHTTP;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        fakeURL = "http://localhost:12345/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl";
        workingURL = new DropUrlGenerator("https://test-drop.qabel.de").generateUrl().toString(); //"https://test-drop.qabel.de/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl";
        networkStatus = new NetworkStatus();
        dHTTP = new DropHTTP();
        connector = new HttpDropConnector(networkStatus, dHTTP);
    }


    @Test(expected = QblNetworkInvalidResponseException.class)
    public void sendDropMessageFailTest() throws Exception {
        Collection<DropURL> collection = new ArrayList<>();
        DropURL drpoUrl = new DropURL(fakeURL);
        collection.add(drpoUrl);
        i = identityBuilderFactory.factory().withAlias("TestAlias").build();
        c = new Contact(i.getAlias(), collection, i.getEcPublicKey());
        DropMessage dropMessage = new DropMessage(i, text, "dropMessage");

        connector.send(c, dropMessage);
    }

    @Test
    public void sendAndReceiveMessagesTest() throws Exception {
        String text = "MessageString";
        String type = "dropMessage";

        Date sinceDate = new Date(0L);
        Collection<DropURL> collection = new ArrayList<>();
        DropURL dropURL = new DropURL(workingURL);
        collection.add(dropURL);

        i = identityBuilderFactory.factory().withAlias("TestAlias").build();
        Identity identity = new Identity("TestAlias", collection, i.getPrimaryKeyPair());
        c = new Contact(identity.getAlias(), collection, identity.getEcPublicKey());

        DropMessage dropMessage = new DropMessage(identity, text, type);
        List<DropMessage> oldMessages = connector.receive(identity, sinceDate);

        connector.send(c, dropMessage);
        List<DropMessage> messages = connector.receive(identity, sinceDate);

        assertEquals(oldMessages.size()+1, messages.size());
        assertEquals(text, messages.get(messages.size()-1).getDropPayload());
        assertEquals(type, messages.get(messages.size()-1).getDropPayloadType());
        assertEquals(c.getEcPublicKey().getReadableKeyIdentifier(), messages.get(messages.size()-1).getSenderKeyId());
    }

    @Test
    public void setsNetworkState() throws Exception {
        StubDropHttp dropStub = new StubDropHttp();
        dropStub.messages.setData(new ArrayList<>());
        connector = new HttpDropConnector(networkStatus, dropStub);
        networkStatus.online();

        dropStub.messages.setResponseCode(0);
        connector.receive(identity, new Date(0L));

        assertFalse(networkStatus.isOnline());

        dropStub.messages.setResponseCode(204);
        connector.receive(identity, new Date(0L));

        assertTrue(networkStatus.isOnline());
    }
}
