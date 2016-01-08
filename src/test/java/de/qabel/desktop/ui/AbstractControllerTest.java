package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.config.Account;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.inmemory.InMemoryIdentityRepository;
import javafx.application.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AbstractControllerTest {
	protected Map<String, Object> diContainer = new HashMap<>();
	protected IdentityRepository identityRepository;

	@BeforeClass
	public static void setUpClass() {
		new Thread() {
			public void run() {
				Application.launch(TestApplication.class);
			}
		}.start();
	}

	@Before
	public void setUp() throws URISyntaxException {
		diContainer.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		diContainer.put("identityBuilderFactory", new IdentityBuilderFactory((DropUrlGenerator) diContainer.get("dropUrlGenerator")));
		diContainer.put("account", new Account("a", "b", "c"));
		identityRepository = new InMemoryIdentityRepository();
		diContainer.put("identityRepository", identityRepository);
		Injector.setConfigurationSource(diContainer::get);
	}

	@After
	public void tearDown() {
		Injector.forgetAll();
	}
}
