package de.qabel.desktop.ui.inject;

import javax.inject.Inject;

public class SomeDefaultInjecttionClass {
	String param;

	@Inject
	public SomeDefaultInjecttionClass(String param) {
		this.param = param;
	}
}
