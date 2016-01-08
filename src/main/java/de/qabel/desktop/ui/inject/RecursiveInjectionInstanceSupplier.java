package de.qabel.desktop.ui.inject;

import com.airhacks.afterburner.injection.Injector;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class RecursiveInjectionInstanceSupplier implements Function<Class<?>, Object> {
	private final Map<String, Object> customProperties;
	private final Map<Class, Object> customPropertiesClassmap = new WeakHashMap<>();

	public RecursiveInjectionInstanceSupplier(Map<String, Object> customProperties) {
		this.customProperties = customProperties;
		for (Object entry : customProperties.values()) {
			customPropertiesClassmap.put(entry.getClass(), entry);
		}
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
					boolean resolved = false;
					if (customPropertiesClassmap.containsKey(parameterClass)) {
						instances.add(customPropertiesClassmap.get(parameterClass));
						continue;
					}
					for (Object value : customProperties.values()) {
						if (parameterClass.isInstance(value)) {
							instances.add(value);
							resolved = true;
							break;
						}
					}
					if (resolved) {
						continue;
					}
					instances.add(Injector.instantiateModelOrService(parameterClass));
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
