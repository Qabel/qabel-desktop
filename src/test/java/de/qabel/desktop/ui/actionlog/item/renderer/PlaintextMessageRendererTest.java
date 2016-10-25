package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.ui.AbstractFxTest;
import javafx.scene.control.Labeled;
import org.controlsfx.control.HyperlinkLabel;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlaintextMessageRendererTest extends AbstractFxTest {
    private PlaintextMessageRenderer renderer;
    private String payload;

    @Before
    public void setUp() throws Exception {
        renderer = new PlaintextMessageRenderer();
        payload = "{'msg': 'content'}";
    }

    @Test
    public void rendersPlaintextFromJson() {
        String message = renderer.renderString(payload, null);
        assertEquals("content", message);
    }

    @Test
    public void rendersMessageNode() {
        String message = renderer.renderString(payload, null);
        Labeled node = renderer.renderLabel(message);
        assertEquals("content", ((Labeled) node).getText());
    }

    @Test
    public void renderHyperlinks() throws Exception {
        final String string = "This is a Text,\n"
            + " wich has a neat hyperlinks www.qabel.de";

        String expectedUriFormat = "[www.qabel.de]";

        AtomicReference<String> browserOpener = new AtomicReference<>();
        renderer.browserOpener = browserOpener::set;

        HyperlinkLabel node = renderer.renderTextFlow("", string);
        assertTrue(node.getText().contains(expectedUriFormat));
    }


}
