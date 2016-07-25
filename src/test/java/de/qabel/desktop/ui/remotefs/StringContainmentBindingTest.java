package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringContainmentBindingTest {
    private BoxObject value = new BoxFile("prefix", "block", "name", 0L, 0L, new byte[0], null, null);
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
        value = new BoxFile("p", "b", "Name", 0L, 0L, new byte[0], null, null);
        filterProperty.setValue("name");
        assertTrue(sut.get());
    }
}
