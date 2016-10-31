package de.qabel.desktop.ui.inject;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.desktop.ServiceFactory;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RecursiveInjectionInstanceSupplier implements Function<Class<?>, Object> {
    private final ServiceFactory serviceFactory;

    public RecursiveInjectionInstanceSupplier(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @Override
    public Object apply(Class<?> aClass) {
        try {
            for (Constructor<?> constructor : aClass.getConstructors()) {
                boolean injectionAnnotated = false;
                for (Annotation annotation : constructor.getDeclaredAnnotations()) {
                    if (annotation.annotationType().equals(Inject.class)) {
                        injectionAnnotated = true;
                        break;
                    }
                }
                if (!injectionAnnotated) {
                    continue;
                }

                List<Object> instances = new LinkedList<>();
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                for (int i = 0; i < constructor.getParameterCount(); i++) {
                    Class<?> parameterClass = parameterTypes[i];
                    Object instance = serviceFactory.getByType(parameterClass);
                    if (instance != null) {
                        instances.add(instance);
                        continue;
                    }
                    try {
                        instances.add(Injector.instantiateModelOrService(parameterClass));
                    } catch (IllegalStateException e) {
                        throw new IllegalStateException("failed to create parameter " + parameterClass.getSimpleName() + " for constructor of " + aClass.getSimpleName(), e);
                    }
                }

                return constructor.newInstance(instances.toArray());
            }
            try {
                return aClass.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException var2) {
                throw new IllegalStateException("Cannot instantiate view: " + aClass.getConstructor(), var2);
            }

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
