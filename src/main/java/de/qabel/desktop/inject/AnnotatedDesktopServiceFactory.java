package de.qabel.desktop.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public abstract class AnnotatedDesktopServiceFactory extends DefaultServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(AnnotatedDesktopServiceFactory.class);
    private Map<String, Method> creators = new HashMap<>();

    public AnnotatedDesktopServiceFactory() {
        List<Class> annotatedClasses = new LinkedList<>();
        annotatedClasses.addAll(Arrays.asList(getClass().getInterfaces()));
        walkSuperclasses(getClass(), annotatedClasses::add);

        for (Class clazz : annotatedClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                method.setAccessible(true);
                if (method.isAnnotationPresent(Create.class)) {
                    String createdInstance = method.getAnnotation(Create.class).name();
                    mapAnnotation(createdInstance, method);
                } else if (method.isAnnotationPresent(Creates.class)) {
                    for (Create create : method.getAnnotation(Creates.class).value()) {
                        mapAnnotation(create.name(), method);
                    }
                }
            }
        }
    }

    private void mapAnnotation(String instanceName, Method method) {
        creators.put(instanceName, method);
        logger.debug("@Create(" + instanceName + ")");
    }

    private void walkSuperclasses(Class clazz, Consumer<Class> visitor) {
        if (clazz.getSuperclass() != Object.class) {
            walkSuperclasses(clazz.getSuperclass(), visitor);
        }
        for(Class i : clazz.getInterfaces()) {
            visitor.accept(i);
        }
        visitor.accept(clazz);
    }

    @Override
    public Object getByType(Class type) {
        Object instance = super.getByType(type);
        if (instance != null) {
            return instance;
        }
        for (Method method : creators.values()) {
            if (type.isAssignableFrom(method.getReturnType())) {
                return invoke(method);
            }
        }
        return null;
    }

    @Override
    public synchronized Object get(String key) {
        if (!cache.containsKey(key)) {
            cache.put(key, generate(key));
        }
        return super.get(key);
    }

    private Object generate(String key) {
        if (!creators.containsKey(key)) {
            throw new IllegalArgumentException("failed to create instance for " + key);
        }

        Method method = creators.get(key);
        return invoke(method);
    }

    private Object invoke(Method method) {
        try {
            return method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("failed to call public method on correct class: " + e.getMessage(), e);
        }
    }
}
