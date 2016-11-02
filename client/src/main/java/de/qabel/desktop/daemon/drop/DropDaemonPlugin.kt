package de.qabel.desktop.daemon.drop

import de.qabel.core.event.EventSource
import de.qabel.desktop.event.ClientStartedEvent
import de.qabel.desktop.inject.CompositeServiceFactory
import de.qabel.desktop.plugin.ClientPlugin
import javax.inject.Inject

class DropDaemonPlugin : ClientPlugin {
    @Inject
    lateinit var dropDaemon: DropDaemon

    override fun initialize(serviceFactory: CompositeServiceFactory, events: EventSource) {
        events.events(ClientStartedEvent::class.java).subscribe { Thread(dropDaemon).start() }
    }

}
