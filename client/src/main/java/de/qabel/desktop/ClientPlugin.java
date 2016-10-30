package de.qabel.desktop;

import de.qabel.core.event.EventSource;
import de.qabel.desktop.inject.CompositeServiceFactory;

public interface ClientPlugin {
    void initialize(CompositeServiceFactory serviceFactory, EventSource events);
    default String getName() {
        return getClass().getName();
    }
}
