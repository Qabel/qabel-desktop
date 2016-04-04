package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import com.sun.javafx.application.PlatformImpl;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.ServiceFactory;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.DefaultClientConfiguration;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.crashReports.StubCrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.BoxVolumeFactoryStub;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.Stub.InMemoryContactRepository;
import de.qabel.desktop.repository.Stub.StubDropMessageRepository;
import de.qabel.desktop.repository.inmemory.InMemoryHttpDropConnector;
import de.qabel.desktop.repository.inmemory.InMemoryIdentityRepository;
import de.qabel.desktop.inject.DefaultServiceFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.application.Application;
import javafx.application.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import static de.qabel.desktop.AsyncUtils.*;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.fail;

public class AbstractControllerTest {
	protected ServiceFactory diContainer = new DefaultServiceFactory();
	protected IdentityRepository identityRepository = new InMemoryIdentityRepository();
	protected DefaultClientConfiguration clientConfiguration;
	protected IdentityBuilderFactory identityBuilderFactory;
	protected ContactRepository contactRepository = new InMemoryContactRepository();
	protected DefaultTransferManager loadManager;
	protected BoxVolumeFactoryStub boxVolumeFactory;
	protected StubDropMessageRepository dropMessageRepository = new StubDropMessageRepository();
	protected DropConnector httpDropConnector = new InMemoryHttpDropConnector();
	protected CrashReportHandler crashReportHandler = new StubCrashReportHandler();
	protected SharingService sharingService = new BlockSharingService(dropMessageRepository, httpDropConnector);
	protected NetworkStatus networkStatus = new NetworkStatus();
	protected Identity identity;

	@BeforeClass
	public static void setUpClass() throws Exception {
		Platform.setImplicitExit(false);
		try {
			Platform.runLater(() -> {
			});
		} catch (IllegalStateException e) {
			startPlatform();
		} catch (Exception e) {
			e.printStackTrace();
			startPlatform();
		}
	}

	private static void startPlatform() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
		new Thread() {
			@Override
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
		identityBuilderFactory = new IdentityBuilderFactory((DropUrlGenerator) diContainer.get("dropUrlGenerator"));
		diContainer.put("identityBuilderFactory", identityBuilderFactory);
		diContainer.put("account", new Account("a", "b", "c"));
		diContainer.put("identityRepository", identityRepository);
		diContainer.put("contactRepository", contactRepository);
		boxVolumeFactory = new BoxVolumeFactoryStub();
		diContainer.put("boxVolumeFactory", boxVolumeFactory);
		loadManager = new DefaultTransferManager();
		diContainer.put("loadManager", loadManager);
		diContainer.put("dropMessageRepository", dropMessageRepository);
		diContainer.put("dropConnector", httpDropConnector);
		diContainer.put("transferManager", new DefaultTransferManager());
		diContainer.put("sharingService", sharingService);
		diContainer.put("reportHandler", crashReportHandler);
		diContainer.put("networkStatus", networkStatus);
		MessageRendererFactory messageRendererFactory = new MessageRendererFactory();
		messageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
		diContainer.put("messageRendererFactory", messageRendererFactory);
		Injector.setConfigurationSource(key -> diContainer.get((String)key));
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(diContainer));

		identity = identityBuilderFactory.factory().withAlias("TestAlias").build();
		clientConfiguration.selectIdentity(identity);
		QabelFXMLView.unloadDefaultResourceBundle();
	}

	@After
	public void tearDown() throws Exception {
		try {
			Injector.forgetAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Function<String, Object> generateInjection(String name, Object instance) {
		return requestedName -> requestedName.equals(name) ? instance : null;
	}

	protected void runLaterAndWait(Runnable runnable) {
		boolean[] hasRun = new boolean[]{false};
		Platform.runLater(() -> {
			runnable.run();
			hasRun[0] = true;
		});
		waitUntil(() -> hasRun[0], 5000L);
	}

	public static void waitUntil(Callable<Boolean> evaluate) {
		AsyncUtils.waitUntil(evaluate);
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		AsyncUtils.waitUntil(evaluate, timeout);
	}

	public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
		AsyncUtils.waitUntil(evaluate, errorMessage);
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
		AsyncUtils.waitUntil(evaluate, timeout, errorMessage);
	}
}
