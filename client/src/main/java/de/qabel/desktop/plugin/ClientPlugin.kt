package de.qabel.desktop.plugin

import de.qabel.core.event.EventSource
import de.qabel.desktop.ServiceFactory
import de.qabel.desktop.inject.CompositeServiceFactory

interface ClientPlugin {
    fun initialize(serviceFactory: CompositeServiceFactory, events: EventSource)
    val name: String
        @JvmSynthetic get() = javaClass.name
}
interface ServiceFactoryProvider {
    fun getServiceFactory() : ServiceFactory
}
