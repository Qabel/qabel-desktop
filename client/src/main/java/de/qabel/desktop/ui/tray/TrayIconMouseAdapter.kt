package de.qabel.desktop.ui.tray

import de.qabel.core.extensions.letApply
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.util.*
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

internal class TrayIconMouseAdapter(private val showApp: Runnable, private val bringAppToFront: Runnable) : MouseAdapter() {
    val popup: JPopupMenu
    var popupCloseCheckDelay = 500
    var popupCloseCheckPeriod = 1500
    private var inBound: Boolean = false
    private var notificationTimer: Timer? = null

    init {
        popup = buildSystemTrayJPopupMenu()

        popup.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent?) {
                inBound = true
            }
        })

        popup.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent) {
                if (isInBounds(e, popup)) {
                    return
                }
                inBound = false
            }

            private fun isInBounds(e: MouseEvent, popup: JPopupMenu): Boolean {
                return e.x < popup.bounds.maxX &&
                        e.x >= popup.bounds.minX &&
                        e.y < popup.bounds.maxY &&
                        e.y >= popup.bounds.minY
            }
        })
    }

    private fun hideIfOutOfBounds() {
        if (popup.isVisible && !inBound) {
            popup.isVisible = false
            stopTimer()
        }
        inBound = false
    }

    @Synchronized private fun startTimer() {
        stopTimer()
        notificationTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    hideIfOutOfBounds()
                }
            }, popupCloseCheckDelay.toLong(), popupCloseCheckPeriod.toLong())
        }
    }

    @Synchronized private fun stopTimer() {
        if (notificationTimer != null) {
            notificationTimer!!.cancel()
            notificationTimer = null
        }
    }

    fun setInBound(inBound: Boolean) {
        this.inBound = inBound
    }

    private fun buildSystemTrayJPopupMenu(): JPopupMenu {
        return  JPopupMenu().apply {
            JMenuItem("Exit").letApply {
                it.addActionListener { ae -> System.exit(0) }
                add(it)
            }
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        if (isSingleLeftClick(e)) {
            bringAppToFront.run()
        } else if (isDoubleLeftClick(e)) {
            showApp.run()
        } else if (isRightClick(e)) {
            val position = calculatePosition(e)
            popup.setLocation(position.x, position.y)
            popup.isVisible = true
            startTimer()
        }
    }

    private fun isRightClick(e: MouseEvent): Boolean {
        return e.button == MouseEvent.BUTTON3
    }

    private fun isSingleLeftClick(e: MouseEvent): Boolean {
        return e.button == MouseEvent.BUTTON1 && e.clickCount == 1
    }

    private fun isDoubleLeftClick(e: MouseEvent): Boolean {
        return e.button == MouseEvent.BUTTON1 && e.clickCount == 2
    }

    private fun calculatePosition(e: MouseEvent): Point {
        val point = e.point
        val bounds = getScreenViewableBounds(getGraphicsDeviceAt(point))
        var x = point.x
        var y = point.y

        if (y < bounds.y) {
            y = bounds.y
        } else if (y > bounds.y + bounds.height) {
            y = bounds.y + bounds.height
        }
        if (x < bounds.x) {
            x = bounds.x
        } else if (x > bounds.x + bounds.width) {
            x = bounds.x + bounds.width
        }
        if (x + popup.width > bounds.x + bounds.width) {
            x = bounds.x + bounds.width - popup.width
        }
        if (y + popup.width > bounds.y + bounds.height) {
            y = bounds.y + bounds.height - popup.height
        }
        return point
    }

    private fun getGraphicsDeviceAt(pos: Point): GraphicsDevice {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val lstGDs = ge.screenDevices
        val lstDevices = ArrayList<GraphicsDevice>(lstGDs.size)

        for (gd in lstGDs) {
            val gc = gd.defaultConfiguration
            val screenBounds = gc.bounds
            if (screenBounds.contains(pos)) {
                lstDevices.add(gd)
            }
        }
        return lstDevices.first()
    }

    private fun getScreenViewableBounds(gd: GraphicsDevice?): Rectangle {
        var bounds = Rectangle(0, 0, 0, 0)
        if (gd != null) {
            val gc = gd.defaultConfiguration
            bounds = gc.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)

            bounds.x += insets.left
            bounds.y += insets.top
            bounds.width -= insets.left + insets.right
            bounds.height -= insets.top + insets.bottom
        }
        return bounds
    }
}
