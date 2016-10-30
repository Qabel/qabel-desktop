package de.qabel.desktop.inject;

import de.qabel.desktop.ServiceFactory;

import java.util.LinkedList;
import java.util.List;

public class CompositeServiceFactory extends DefaultServiceFactory {
    private List<ServiceFactory> serviceFactories = new LinkedList<>();

    @Override
    public Object get(String key) {
        if (super.get(key) == null) {
            for (ServiceFactory factory : serviceFactories) {
                try {
                    Object instance = factory.get(key);
                    if (instance != null) {
                        put(key, instance);
                        break;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return super.get(key);
    }

    @Override
    public Object getByType(Class type) {
        for (ServiceFactory factory : serviceFactories) {
            Object instance = factory.getByType(type);
            if (instance != null) {
                return instance;
            }
        }
        return super.getByType(type);
    }

    public void addServiceFactory(ServiceFactory serviceFactory) {
        serviceFactories.add(serviceFactory);
    }
}
