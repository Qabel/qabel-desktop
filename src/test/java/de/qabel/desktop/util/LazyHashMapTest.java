package de.qabel.desktop.util;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
