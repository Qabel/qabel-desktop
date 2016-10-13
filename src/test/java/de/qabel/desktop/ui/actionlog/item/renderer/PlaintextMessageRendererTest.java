package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.ui.AbstractFxTest;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.text.TextFlow;
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
        Node node = renderer.render(payload, null);
        assertTrue(node instanceof Labeled);
        assertEquals("content", ((Labeled) node).getText());
    }

    @Test
    public void renderHyperlinks() throws Exception {
        final String string = "This is a Text,\n"
            + " wich has a neat hyperlinks www.qabel.de";

        String expectedUriFormat = "[www.qabel.de]";

        AtomicReference<String> browserOpener = new AtomicReference<>();
        renderer.browserOpener = browserOpener::set;

        Node node = renderer.renderHyperlinks(string);
        HyperlinkLabel hyperLinkLabel = ((HyperlinkLabel) ((TextFlow) node).getChildren().get(0));
        assertTrue(hyperLinkLabel.getText().contains(expectedUriFormat));
    }


}
