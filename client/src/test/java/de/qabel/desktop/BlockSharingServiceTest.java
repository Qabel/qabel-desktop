package de.qabel.desktop;

import de.qabel.box.storage.BoxExternalReference;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropMessageMetadata;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlockSharingServiceTest extends AbstractControllerTest {

    @Test
    public void shareAndSendMessage() throws Exception {
        BlockSharingService service = new BlockSharingService(dropMessageRepository, httpDropConnector);
        DropMessage message = service.makeDropMessage(identity, "message",
            new BoxExternalReference(false, "", "", identity.getEcPublicKey(), new byte[]{1}));
        assertEquals(new DropMessageMetadata(identity), message.getDropMessageMetadata());
    }

}
