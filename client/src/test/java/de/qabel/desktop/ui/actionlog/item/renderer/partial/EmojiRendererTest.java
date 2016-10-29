package de.qabel.desktop.ui.actionlog.item.renderer.partial;

import com.vdurmont.emoji.EmojiManager;
import de.qabel.desktop.ui.AbstractFxTest;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EmojiRendererTest extends AbstractFxTest {
    public static final String EMOJI = EmojiManager.getForAlias("smiley").getUnicode();
    private EmojiRenderer emojiRenderer = new EmojiRenderer();

    @Test
    public void rendersSingleEmoji() {
        List<Node> nodes = emojiRenderer.render(EMOJI);
        assertEquals(1, nodes.size());
        ImageView image = assertImageView(nodes.get(0));

        assertTrue(image.getStyleClass().contains("emoji"));
        assertEquals(24, image.getFitWidth(), 0.001);
    }

    private ImageView assertImageView(Node actual) {
        assertThat(actual, instanceOf(Label.class));
        Node imageView = ((Label) actual).getGraphic();
        assertThat(imageView, instanceOf(ImageView.class));
        return (ImageView)imageView;
    }


    private void assertText(Node actual) {
        assertThat(actual, instanceOf(Text.class));
    }

    @Test
    public void rendersEmojiTextSequence() {
        List<Node> nodes = emojiRenderer.render("text with " + EMOJI + " and another " + EMOJI);
        assertEquals(4, nodes.size());
        assertText(nodes.get(0));
        assertImageView(nodes.get(1));
        assertText(nodes.get(2));
        assertImageView(nodes.get(3));
    }
}
