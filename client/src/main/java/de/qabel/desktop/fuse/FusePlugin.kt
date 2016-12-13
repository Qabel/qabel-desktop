package de.qabel.desktop.fuse

import de.qabel.box.storage.factory.BoxVolumeFactory
import de.qabel.core.accounting.BoxClient
import de.qabel.core.event.EventSource
import de.qabel.desktop.config.AccountSelectedEvent
import de.qabel.desktop.config.ClientConfig
import de.qabel.desktop.inject.CompositeServiceFactory
import de.qabel.desktop.plugin.ClientPlugin
import java.nio.file.Paths

class FusePlugin : ClientPlugin {
    override fun initialize(serviceFactory: CompositeServiceFactory, events: EventSource) {
        events.events(AccountSelectedEvent::class.java).subscribe { event ->
            try {
                val identity = (serviceFactory.get("config") as ClientConfig).selectedIdentity
                val boxVolumeFactory = serviceFactory.get("boxVolumeFactory") as BoxVolumeFactory
                val volume = boxVolumeFactory.getVolume(event.account, identity)
                object : Thread() {
                    override fun run() {
                        try {
                            while (!isInterrupted) {
                                Thread.sleep(15000)
                                volume.navigate().refresh(true)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }.start()
                object : Thread() {
                    override fun run() {
                        try {
                            val boxFS = BoxFS(volume, serviceFactory.get("boxClient") as BoxClient)
                            try {
                                boxFS.mount(Paths.get("/tmp/test/mnt"), true, true)
                            } finally {
                                boxFS.umount()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
