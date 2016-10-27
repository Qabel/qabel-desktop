package de.qabel.desktop.config.factory;

import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class IdentityBuilderTest {
    private IdentityBuilder builder;

    @Before
    public void setUp() throws URISyntaxException {
        builder = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000"));
    }

    @Test
    public void testAddsGivenValues() throws URISyntaxException, QblDropInvalidURL {
        QblECKeyPair keyPair = new QblECKeyPair();
        DropURL dropUrl = new DropURL("http://nowhere/1234567890123456789012345678901234567890123");
        Identity identity = builder
                .withAlias("my identity")
                .dropAt(dropUrl)
                .encryptWith(keyPair)
                .build();

        assertNotNull(identity);
        assertEquals("my identity", identity.getAlias());
        assertEquals(1, identity.getDropUrls().size());
        assertTrue(identity.getDropUrls().contains(dropUrl));
    }

    @Test
    public void defaultsToSelfGeneratedDropUrlAndDefaultKeyPair() throws Exception {
        Identity identity = builder.withAlias("alias").build();
        assertFalse("no default dropUrl generated", identity.getDropUrls().isEmpty());
        assertNotNull("no default key pair generated", identity.getPrimaryKeyPair());
    }
}
