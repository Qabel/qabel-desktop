package de.qabel.desktop.ui.tray

class TrayProxy : QabelTray {
    var instance: QabelTray? = null

    override fun install() = instance?.install() ?: Unit

    override fun showNotification(trayNotification: TrayNotification) = instance?.showNotification(trayNotification)
        ?: throw IllegalArgumentException("notification must not be null")
}
