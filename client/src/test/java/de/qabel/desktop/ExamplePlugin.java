package de.qabel.desktop;

import de.qabel.core.event.EventSource;
import de.qabel.desktop.inject.CompositeServiceFactory;

public class ExamplePlugin implements ClientPlugin {
    public static boolean initialized;

    @Override
    public void initialize(CompositeServiceFactory serviceFactory, EventSource events) {
        initialized = true;
    }
}
