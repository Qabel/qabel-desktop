package de.qabel.desktop;

import de.qabel.box.storage.BoxExternalReference;
import de.qabel.box.storage.BoxFile;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DefaultContactFactory;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropMessageMetadata;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockSharingServiceTest extends AbstractControllerTest {

    @Test
    public void shareAndSendMessage() throws Exception {
        BlockSharingService service = new BlockSharingService(dropMessageRepository, httpDropConnector);
        Identity i = identityBuilderFactory.factory().withAlias("TestAlias").build();
        Contact c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
        DropMessage message = service.makeDropMessage(identity, c, "message",
            new BoxExternalReference(false, "", "", identity.getEcPublicKey(), new byte[]{1}));
        assertEquals(new DropMessageMetadata(identity), message.getDropMessageMetadata());
    }

}
