package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringContainmentBindingTest {
	private BoxObject value = new BoxFile("prefix", "block", "name", 0L, 0L, new byte[0]);
	private StringProperty filterProperty = new SimpleStringProperty("");
	private StringContainmentBinding sut = new StringContainmentBinding(filterProperty, () -> value);

	@Test
	public void isFalseOnInvalidFilter() {
		filterProperty.setValue("test");
		assertFalse(sut.get());
	}

	@Test
	public void isTrueOnCaseSensitiveMatch() {
		filterProperty.setValue("name");
		assertTrue(sut.get());
	}

	@Test
	public void isTrueOnCaseInsensitiveMatchFromFilter() {
		filterProperty.setValue("Name");
		assertTrue(sut.get());
	}

	@Test
	public void isTrueOnCaseInsensitiveMatchFromValue() {
		value = new BoxFile("p", "b", "Name", 0L, 0L, new byte[0]);
		filterProperty.setValue("name");
		assertTrue(sut.get());
	}
}
