package de.qabel.desktop.util;

public interface CheckedFunction<T, S> {
    S apply(T t) throws Exception;
}
