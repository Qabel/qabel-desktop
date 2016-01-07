package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.repository.persistence.PersistenceAccountRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.ui.accounting.AccountingView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;
import java.util.function.Function;


public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);

		final Map<Object, Object> customProperties = new HashMap<>();
		Persistence<String> persistence = new SQLitePersistence("qabel-desktop.sqlite", "qabel".toCharArray());
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		customProperties.put("identityRepository", new PersistenceIdentityRepository(persistence));
		customProperties.put("accountRepository", new PersistenceAccountRepository(persistence));
		customProperties.put("account", getBoxAccount());
		Injector.setConfigurationSource(customProperties::get);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(customProperties));

		Scene accountingScene = new Scene(new AccountingView().getView(), 800, 600);
		primaryStage.setScene(accountingScene);

		primaryStage.setTitle(TITLE);
		primaryStage.show();
	}

	public static Account getBoxAccount() {
		return new Account("http://localhost:9696", "testuser", "pbkdf2_sha256$20000$nrvtwYIbo9O0$Z2CEW5MbUM6geod1e35pAdI2YLFzcQbikk\\/fktMfq18=");
	}

	private class RecursiveInjectionInstanceSupplier implements Function<Class<?>, Object> {
		private final Map<Object, Object> customProperties;
		private final Map<Class, Object> customPropertiesClassmap = new WeakHashMap<>();

		public RecursiveInjectionInstanceSupplier(Map<Object, Object> customProperties) {
			this.customProperties = customProperties;
			for (Object entry : customProperties.values()) {
				customPropertiesClassmap.put(entry.getClass(), entry);
			}
		}

		@Override
		public Object apply(Class<?> aClass) {
			System.out.println("resolving class " + aClass.getSimpleName());
			try {
				for (Constructor<?> constructor : aClass.getConstructors()) {
					System.out.println("trying constructor " + constructor.getName() + " with " + constructor.getParameterCount() + " parameters");
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
						System.out.println("resolving parameter " + i + " of type " + parameterClass.getSimpleName());
						boolean resolved = false;
						if (customPropertiesClassmap.containsKey(parameterClass)) {
							instances.add(customPropertiesClassmap.get(parameterClass));
							continue;
						}
						for (Object value : customProperties.values()) {
							System.out.println("trying to use " + value);
							if (parameterClass.isInstance(value)) {
								System.out.println("using " + value);
								instances.add(value);
								resolved = true;
								break;
							}
						}
						if (resolved) {
							continue;
						}
						System.out.println("Failed to resolve parameter " + i + ", trying magic construction");
						instances.add(Injector.instantiateModelOrService(parameterClass));
					}

					for(Object instance : instances) {
						System.out.println("resolved parameter: " + instance.getClass().getSimpleName());
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
}
