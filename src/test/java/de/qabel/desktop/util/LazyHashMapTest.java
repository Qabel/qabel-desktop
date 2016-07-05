package de.qabel.desktop.util;

import de.qabel.core.util.LazyHashMap;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LazyHashMapTest {
    @Test
    public void usesFactoryForDefault() {
        LazyHashMap<String, String> map = new LazyHashMap<>();
        assertThat(map.getOrDefault("new key", s -> "new value"), is(equalTo("new value")));
        assertThat(
            map.getOrDefault("new key", s -> {fail("called unneccessaruly"); return "false";}),
            is(equalTo("new value"))
        );
    }
}
