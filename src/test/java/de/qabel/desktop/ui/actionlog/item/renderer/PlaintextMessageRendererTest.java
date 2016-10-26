package de.qabel.desktop.ui.actionlog.item.renderer;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.desktop.ui.AbstractFxTest;
import de.qabel.desktop.ui.actionlog.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

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
        TextFlow node = renderer.render("alias", payload, QabelFXMLView.getDefaultResourceBundle());
        assertEquals("content", ((Text)node.getChildren().get(1)).getText());
    }

    @Test
    public void rendersNodes() {
        TextFlow flow = renderer.render("alias", createPayload("contains http://qabel.de"), null);
        assertThat(flow.getChildren().size(), equalTo(3));
        assertThat(flow.getChildren().get(2), instanceOf(Hyperlink.class));
    }

    private String createPayload(String message) {
        return "{'msg': '" + message.replace("'", "\\'") + "'}";
    }
}
