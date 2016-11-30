package de.qabel.desktop.ui.actionlog.item.renderer.partial

import com.vdurmont.emoji.EmojiManager
import de.qabel.desktop.ui.AbstractFxTest
import de.qabel.desktop.ui.util.StaticIconProvider
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.text.Text
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Test

class EmojiRendererTest : AbstractFxTest() {
    private val EMOJI = EmojiManager.getForAlias("smiley").unicode
    private val emojiRenderer = EmojiRenderer(StaticIconProvider())

    @Test
    fun rendersSingleEmoji() {
        val nodes = emojiRenderer.render(EMOJI)
        assertEquals(1, nodes.size)
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
        assertEquals(4, nodes.size)
        assertText(nodes[0])
        assertImageView(nodes[1])
        assertText(nodes[2])
        assertImageView(nodes[3])
    }

    @Test
    fun usesFallbackRenderingOnException() {
        val nodes = emojiRenderer.render(":meh:").apply { assertEquals(1, this.size) }
        val node = nodes[0]
        assertThat(node, instanceOf<Node>(Text::class.java))
        assertEquals(":meh:", (node as Text).text)
    }
}
