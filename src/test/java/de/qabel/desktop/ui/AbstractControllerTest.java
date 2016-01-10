package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import com.sun.javafx.application.PlatformImpl;
import de.qabel.core.config.Account;
import de.qabel.desktop.config.DefaultClientConfiguration;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.inmemory.InMemoryIdentityRepository;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.application.Application;
import javafx.application.Platform;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.fail;

public class AbstractControllerTest {
	protected Map<String, Object> diContainer = new HashMap<>();
	protected IdentityRepository identityRepository = new InMemoryIdentityRepository();
	protected DefaultClientConfiguration clientConfiguration;

	@BeforeClass
	public static void setUpClass() throws Exception {
		try {
			Platform.runLater(() -> {});
		} catch (IllegalStateException e) {
			startPlatform();
		} catch (Exception e) {
			e.printStackTrace();
			startPlatform();
		}
	}

	private static void startPlatform() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
		new Thread() {
			public void run() {
				Application.launch(TestApplication.class);
			}
		}.start();
		Field field = PlatformImpl.class.getDeclaredField("initialized");
		field.setAccessible(true);
		while (!((AtomicBoolean) field.get(null)).get())
			Thread.sleep(10);
	}

	@Before
	public void setUp() throws Exception {
		clientConfiguration = new DefaultClientConfiguration();
		diContainer.put("clientConfiguration", clientConfiguration);
		diContainer.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		diContainer.put("identityBuilderFactory", new IdentityBuilderFactory((DropUrlGenerator) diContainer.get("dropUrlGenerator")));
		diContainer.put("account", new Account("a", "b", "c"));
		diContainer.put("identityRepository", identityRepository);
		Injector.setConfigurationSource(diContainer::get);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(diContainer));
	}

	protected static void waitUntil(Callable<Boolean> evaluate) {
		waitUntil(evaluate, 1000L);
	}

	protected static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		long startTime = System.currentTimeMillis();
		try {
			while (!evaluate.call()) {
				Thread.yield();
				if (System.currentTimeMillis() - timeout > startTime) {
					fail("wait timeout");
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@After
	public void tearDown() {
		try {
			Injector.forgetAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Function<String, Object> generateInjection(String name, Object instance) {
		return (requestedName) -> requestedName.equals(name) ? instance : null;
	}

	protected void runLaterAndWait(Runnable runnable) {
		boolean[] hasRun = new boolean[]{false};
		Platform.runLater(() -> {
			runnable.run();
			hasRun[0] = true;
		});
		waitUntil(() -> hasRun[0]);
	}
}
