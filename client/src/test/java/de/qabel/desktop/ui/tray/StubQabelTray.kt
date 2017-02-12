package de.qabel.desktop.ui.tray

class StubQabelTray : QabelTray {
    var installed = false
    var notifications = mutableListOf<TrayNotification>()

    override fun install() {
        installed = true
    }

    override fun showNotification(trayNotification: TrayNotification) {
        notifications.add(trayNotification)
    }
}
