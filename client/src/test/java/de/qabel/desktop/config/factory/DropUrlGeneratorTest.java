package de.qabel.desktop.config.factory;

import de.qabel.core.config.factory.DropUrlGenerator;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

public class DropUrlGeneratorTest {
    @Test
    public void genenratesPrefixedUrl() throws URISyntaxException {
        String prefix = "http://localhost:5000";
        DropUrlGenerator generator = new DropUrlGenerator(prefix);
        String generatedUrl = generator.generateUrl().getUri().toString();
        assertTrue(generatedUrl.startsWith(prefix + "/"));
        assertTrue(generatedUrl.length() > prefix.length() + 1);
    }

    @Test
    public void usesDynamicPrefix() throws URISyntaxException {
        String otherPrefix = "http://example.org";
        DropUrlGenerator generator = new DropUrlGenerator(otherPrefix);
        String generatedUrl = generator.generateUrl().getUri().toString();
        assertTrue(generatedUrl.startsWith(otherPrefix));
    }
}
