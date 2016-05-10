package de.qabel.desktop.util;

import java.util.Map;
import java.util.function.Function;

public interface LazyMap<K, V> extends Map<K, V> {
    default V getOrDefault(K key, Function<K, V> defaultValueFactory) {
        synchronized (this) {
            if (!containsKey(key)) {
                put(key, defaultValueFactory.apply(key));
            }
            return get(key);
        }
    }
}
