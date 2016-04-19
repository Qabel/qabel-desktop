package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.ui.AbstractFxTest;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlaintextMessageRendererTest extends AbstractFxTest {
    private PlaintextMessageRenderer renderer;

    @Before
    public void setUp() throws Exception {
        renderer = new PlaintextMessageRenderer();
    }

    @Test
    public void rendersPlaintextFromJson() {
        String payload =  "{'msg': 'content'}";
        String message = renderer.renderString(payload, null);

        assertEquals("content", message);
    }

    @Test
    public void rendersMessageNode() {
        String payload =  "{'msg': 'content'}";
        Node node = renderer.render(payload, null);
        assertTrue(node instanceof Labeled);
        assertEquals("content", ((Labeled)node).getText());
    }
}
