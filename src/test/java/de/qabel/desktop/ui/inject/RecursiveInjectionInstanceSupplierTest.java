package de.qabel.desktop.ui.inject;

import com.airhacks.afterburner.injection.Injector;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RecursiveInjectionInstanceSupplierTest {
	private Map<String, Object> properties = new HashMap<>();

	@After
	public void tearDown() {
		Injector.forgetAll();
	}

	@Test
	public void injectsWithDefaultConstructor() {
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(properties));
		SomeDefaultInjecttionClass instance = Injector.instantiateModelOrService(SomeDefaultInjecttionClass.class);
		assertNotNull(instance.param);
	}

	@Test
	public void injectsConstructorRecursiveAndFromGlobalContext() {
		SomeClass injectableInstance = new SomeClass(5);
		properties.put("someClass", injectableInstance);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(properties));

		SomeInjectionClass instance = Injector.instantiateModelOrService(SomeInjectionClass.class);
		assertSame(injectableInstance, instance.someClass);
		assertNotNull(instance.anotherClass.param);
	}

	@Test
	public void injectsSubclasses() {
		SomeClass injectableInstance = new SomeSubclass(99);
		properties.put("someClass", injectableInstance);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(properties));

		SomeInjectionClass instance = Injector.instantiateModelOrService(SomeInjectionClass.class);
		assertSame(injectableInstance, instance.someClass);
	}
}
