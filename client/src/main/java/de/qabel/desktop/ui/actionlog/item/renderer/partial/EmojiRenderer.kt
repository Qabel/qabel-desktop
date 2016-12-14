package de.qabel.desktop.ui.actionlog.item.renderer.partial

import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser
import de.qabel.core.logging.QabelLog
import de.qabel.desktop.ui.util.IconProvider
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

class EmojiRenderer(val icons: IconProvider) : AbstractPatternExtractionRenderer(), QabelLog {
    private val ALIAS_PATTERN = Pattern.compile("(:(\\w|\\||\\-)+:)")

    override fun needsFormatting(text: String) = super.needsFormatting(EmojiParser.parseToAliases(text))
    override fun getPattern() = ALIAS_PATTERN
    override fun render(text: String) = super.render(EmojiParser.parseToAliases(text))

    override fun replace(match: String, matcher: Matcher): Node {
        if (!match.startsWith(":") || !match.endsWith(":")) {
            return Text(match)
        }

        try {
            val icon = icons.getIcon(EmojiManager.getForAlias(match), 24)
            icon.styleClass.add("emoji")
            val emojiLabel = Label("", icon)
            emojiLabel.styleClass.add("emoji-container")
            emojiLabel.padding = Insets(0.0, 1.0, 0.0, 1.0)
            return emojiLabel
        } catch (e: Exception) {
            error("failed to render emoji " + match, e)
            return renderFallback(match)
        }

    }

    private fun renderFallback(match: String): Text = Text(EmojiParser.parseToUnicode(match))
}
