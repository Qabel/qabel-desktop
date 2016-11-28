package de.qabel.desktop.ui.actionlog.item.renderer.partial

import com.vdurmont.emoji.EmojiManager
import de.qabel.desktop.ui.AbstractFxTest
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.text.Text
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Test

class EmojiRendererTest : AbstractFxTest() {
    private val EMOJI = EmojiManager.getForAlias("smiley").unicode
    private val emojiRenderer = EmojiRenderer()

    @Test
    fun rendersSingleEmoji() {
        val nodes = emojiRenderer.render(EMOJI)
        assertEquals(1, nodes.size.toLong())
        val image = assertImageView(nodes[0])

        assertTrue(image.styleClass.contains("emoji"))
        assertEquals(24.0, image.fitWidth, 0.001)
    }

    private fun assertImageView(actual: Node): ImageView {
        assertThat(actual, instanceOf<Node>(Label::class.java))
        val imageView = (actual as Label).graphic
        assertThat(imageView, instanceOf<Node>(ImageView::class.java))
        return imageView as ImageView
    }


    private fun assertText(actual: Node) = assertThat(actual, instanceOf<Node>(Text::class.java))

    @Test
    fun rendersEmojiTextSequence() {
        val nodes = emojiRenderer.render("text with $EMOJI and another $EMOJI")
        assertEquals(4, nodes.size.toLong())
        assertText(nodes[0])
        assertImageView(nodes[1])
        assertText(nodes[2])
        assertImageView(nodes[3])
    }
}
