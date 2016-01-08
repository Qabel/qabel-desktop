package de.qabel.desktop.ui.inject;

import javax.inject.Inject;

public class SomeInjectionClass {

	SomeClass someClass;

	SomeDefaultInjecttionClass anotherClass;

	@Inject
	public  SomeInjectionClass(SomeClass someClass, SomeDefaultInjecttionClass anotherClass) {
		this.someClass = someClass;
		this.anotherClass = anotherClass;
	}
}
