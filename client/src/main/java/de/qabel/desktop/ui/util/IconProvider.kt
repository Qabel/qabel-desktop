package de.qabel.desktop.ui.util

import com.vdurmont.emoji.Emoji
import javafx.scene.image.Image
import javafx.scene.image.ImageView

interface IconProvider {
    fun getIcon(path: String, width: Int = Icons.LARGE_ICON_WIDTH): ImageView
    fun iconFromImage(image: Image, width: Int = Icons.LARGE_ICON_WIDTH): ImageView
    fun getImage(path: String): Image
    fun getIcon(emoji: Emoji, width: Int = Icons.LARGE_ICON_WIDTH): ImageView
}

class StaticIconProvider : IconProvider {
    override fun getIcon(path: String, width: Int) = Icons.getIcon(path, width)
    override fun iconFromImage(image: Image, width: Int) = Icons.iconFromImage(image, width)
    override fun getImage(path: String) = Icons.getImage(path)
    override fun getIcon(emoji: Emoji, width: Int) = Icons.getIcon(emoji, width)
}
