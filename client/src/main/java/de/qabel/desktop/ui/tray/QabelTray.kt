package de.qabel.desktop.ui.tray

interface QabelTray {
    fun install()
    fun showNotification(trayNotification: TrayNotification)
}
class TrayNotification(val title: String, val content: String)
