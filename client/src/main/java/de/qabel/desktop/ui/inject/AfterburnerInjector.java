package de.qabel.desktop.ui.inject;

import com.airhacks.afterburner.configuration.Configurator;
import com.airhacks.afterburner.injection.PresenterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AfterburnerInjector implements PresenterFactory {
    private static final Logger logger = LoggerFactory.getLogger(AfterburnerInjector.class);
    private static final Map<Class<?>, Object> modelsAndServices = new WeakHashMap<>();
    private static final Set<Object> presenters = Collections.newSetFromMap(new WeakHashMap<>());
    private static Function<Class<?>, Object> instanceSupplier = getDefaultInstanceSupplier();
    private static Consumer<String> LOG = getDefaultLogger();
    private static final Configurator configurator = new Configurator();

    @Override
    public <T> T instantiatePresenter(Class<T> clazz, Function<String, Object> injectionContext) {
        return registerExistingAndInject((T) instanceSupplier.apply(clazz), injectionContext);
    }

    public static void setInstanceSupplier(Function<Class<?>, Object> instanceSupplier) {
        AfterburnerInjector.instanceSupplier = instanceSupplier;
    }

    public static void setConfigurationSource(Function<Object, Object> configurationSupplier) {
        configurator.set(configurationSupplier);
    }

    private static void resetInstanceSupplier() {
        instanceSupplier = getDefaultInstanceSupplier();
    }

    private static void resetConfigurationSource() {
        configurator.forgetAll();
    }

    private static <T> T registerExistingAndInject(T instance, Function<String, Object> additionalInjectionContext) {
        T product = injectAndInitialize(instance, additionalInjectionContext);
        presenters.add(product);
        return product;
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiateModelOrService(Class<T> clazz) {
        T product = (T) modelsAndServices.get(clazz);
        if (product == null) {
            product = injectAndInitialize((T) instanceSupplier.apply(clazz));
            modelsAndServices.putIfAbsent(clazz, product);
        }
        return clazz.cast(product);
    }

    private static <T> T injectAndInitialize(T product) {
        return injectAndInitialize(product, null);
    }

    private static <T> T injectAndInitialize(T product, Function<String, Object> additionalInjectionContext) {
        injectMembers(product, additionalInjectionContext);
        initialize(product);
        return product;
    }

    public static void injectMembers(final Object instance) {
        injectMembers(instance, null);
    }
    public static void injectMembers(final Object instance, Function<String, Object> additionalInjectionContext) {
        Class<?> clazz = instance.getClass();
        injectMembers(clazz, instance, additionalInjectionContext);
    }

    private static void injectMembers(Class<?> clazz, final Object instance) throws SecurityException {
        injectMembers(clazz, instance, null);
    }

    private static void injectMembers(Class<?> clazz, final Object instance, Function<String, Object> additionalInjectionContext) throws SecurityException {
        LOG.accept("Injecting members for class " + clazz + " and instance " + instance);
        Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                LOG.accept("Field annotated with @Inject found: " + field);
                Class<?> type = field.getType();
                String key = field.getName();
                Object value = null;
                if (additionalInjectionContext != null) {
                    value = additionalInjectionContext.apply(key);
                }
                if (additionalInjectionContext == null || value == null) {
                    try {
                        value = configurator.getProperty(clazz, key);
                        LOG.accept("Value returned by configurator is: " + value);
                    } catch (IllegalArgumentException e) {
                        LOG.accept("configurator could not create instance for " + clazz);
                    }
                    if (value == null && isNotPrimitiveOrString(type)) {
                        LOG.accept("Field is not a JDK class");
                        try {
                            value = instantiateModelOrService(type);
                        } catch (IllegalStateException e) {
                            throw new IllegalStateException("failed to inject " + field.getName() + " into " + clazz.getSimpleName(), e);
                        }
                    }
                }

                if (value != null) {
                    LOG.accept("Value is a primitive, injecting...");
                    injectIntoField(field, instance, value);
                }
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            LOG.accept("Injecting members of: " + superclass);
            injectMembers(superclass, instance);
        }
    }

    private static void injectIntoField(final Field field, final Object instance, final Object target) {
        AccessController.doPrivileged((PrivilegedAction<?>) () -> {
            boolean wasAccessible = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(instance, target);
                return null; // return nothing...
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException("Cannot set field: " + field + " with value " + target, ex);
            } finally {
                field.setAccessible(wasAccessible);
            }
        });
    }

    private static void initialize(Object instance) {
        Class<?> clazz = instance.getClass();
        invokeMethodWithAnnotation(clazz, instance, PostConstruct.class
        );
    }

    private static void destroy(Object instance) {
        Class<?> clazz = instance.getClass();
        invokeMethodWithAnnotation(clazz, instance, PreDestroy.class
        );
    }

    private static void invokeMethodWithAnnotation(Class<?> clazz, final Object instance, final Class<? extends Annotation> annotationClass) throws IllegalStateException, SecurityException {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if (method.isAnnotationPresent(annotationClass)) {
                AccessController.doPrivileged((PrivilegedAction<?>) () -> {
                    boolean wasAccessible = method.isAccessible();
                    try {
                        method.setAccessible(true);
                        return method.invoke(instance);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new IllegalStateException("Problem invoking " + annotationClass + " : " + method, ex);
                    } finally {
                        method.setAccessible(wasAccessible);
                    }
                });
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            invokeMethodWithAnnotation(superclass, instance, annotationClass);
        }
    }

    public static void forgetAll() {
        Collection<Object> values = modelsAndServices.values();
        values.stream().forEach(AfterburnerInjector::destroy);
        presenters.stream().forEach(AfterburnerInjector::destroy);
        presenters.clear();
        modelsAndServices.clear();
        resetInstanceSupplier();
        resetConfigurationSource();
    }

    private static Function<Class<?>, Object> getDefaultInstanceSupplier() {
        return c -> {
            try {
                return c.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalStateException("Cannot instantiate view: " + c, ex);
            }
        };
    }

    private static Consumer<String> getDefaultLogger() {
        return logger::debug;
    }

    private static boolean isNotPrimitiveOrString(Class<?> type) {
        return !type.isPrimitive() && !type.isAssignableFrom(String.class);
    }
}
