package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertTrue;

public class HyperlinkRendererTest {
    private HyperlinkRenderer renderer = new HyperlinkRenderer();
    private AtomicReference<String> lastHyperlink = new AtomicReference<>();

    @Test
    public void splitsAroundHyperlink() {
        List<Node> nodes = renderer.render("prefix http://qabel.de suffix");

        assertThat(nodes, hasSize(3));
        assertThat(nodes.get(0), instanceOf(Text.class));
        assertThat(nodes.get(1), instanceOf(Text.class));
        assertThat(nodes.get(2), instanceOf(Text.class));

        assertThat(((Text) nodes.get(0)).getText(), equalTo("prefix "));
        assertThat(((Text) nodes.get(1)).getText(), equalTo("http://qabel.de"));
        assertThat(((Text) nodes.get(2)).getText(), equalTo(" suffix"));
    }

    @Test
    public void splitsMultipleHyperlinks() {
        List<Node> nodes = renderer.render("prefix http://qabel.de and http://qabel.org suffix");

        assertText(nodes.get(0), "prefix ");
        assertText(nodes.get(1), "http://qabel.de");
        assertText(nodes.get(2), " and ");
        assertText(nodes.get(3), "http://qabel.org");
        assertText(nodes.get(4), " suffix");
    }

    private void assertText(Node node, String text) {
        assertThat(node, instanceOf(Text.class));
        assertThat(((Text) node).getText(), equalTo(text));
    }

    @Test
    public void createsHyperlinks() {
        renderer.browserOpener = lastHyperlink::set;

        Text link = (Text)renderer.render("https://qabel.de").get(0);
        assertHyperlink(link);
        click(link);

        assertThat(lastHyperlink.get(), equalTo("https://qabel.de"));
    }

    @Test
    public void parsesNonHttpSchemes() {
        Text link = (Text) renderer.render("ftp://qabel.de").get(0);
        assertHyperlink(link);
    }

    @Test
    public void rendersSpaces() {
        List<Node> nodes = renderer.render("http://qabel.de http://qabel.de");
        assertThat(nodes, hasSize(3));
        assertThat(((Text)nodes.get(1)).getText(), equalTo(" "));
    }

    @Test
    public void detectsUrlWithSubdomainWithoutSchema() {
        assertRendersHyperlink("www.qabel.de");
    }

    @Test
    public void detectsUrlWithoutSchema() {
        assertRendersHyperlink("qabel.de");
    }

    @Test
    public void detectsNewTldWithoutSchema() {
        assertRendersHyperlink("qabel.computer");
    }

    @Test
    public void rejectsSchemalessUrlWithInvalidTLD() {
        assertRendersNoHyperlink("qabel.sooooinvalid");
    }

    @Test
    public void allowsIps() {
        assertRendersHyperlink("http://192.168.1.1:8080/index.html");
    }

    @Test
    public void allowsLocalhostWithScheme() {
        assertRendersHyperlink("http://localhost");
    }

    private void assertRendersHyperlink(String text) {
        assertTrue(renderer.needsFormatting(text));
        List<Node> nodes = renderer.render(text);
        Text link = (Text) nodes.get(0);
        assertHyperlink(link);
        assertThat(nodes, hasSize(1));
    }

    private void assertRendersNoHyperlink(String text) {
        Text link = (Text) renderer.render(text).get(0);
        assertNoHyperlink(link);
    }


    protected void assertHyperlink(Text link) {
        assertTrue(link.getStyleClass().contains("hyperlink"));
    }
    protected void assertNoHyperlink(Text link) {
        assertFalse(link.getStyleClass().contains("hyperlink"));
    }

    protected void click(Text link) {
        link.getOnMouseClicked().handle(mouseEvent(link));
    }

    @NotNull
    protected MouseEvent mouseEvent(Text link) {
        return new MouseEvent(null, link, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
            false, false, false, false, false, false, false, true, false, false, null);
    }
}
