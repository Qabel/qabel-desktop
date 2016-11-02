package de.qabel.desktop.daemon.sync

import de.qabel.core.event.EventSource
import de.qabel.desktop.config.AccountSelectedEvent
import de.qabel.desktop.inject.CompositeServiceFactory
import de.qabel.desktop.plugin.ClientPlugin
import javax.inject.Inject

class SyncPlugin : ClientPlugin {
    @Inject
    lateinit var syncDaemon: SyncDaemon

    override fun initialize(serviceFactory: CompositeServiceFactory, events: EventSource) {
        events.events(AccountSelectedEvent::class.java).subscribe { Thread(syncDaemon).start() }
    }
}
