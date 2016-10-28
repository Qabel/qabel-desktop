package de.qabel.desktop.util;

public interface CheckedCallable<V>  {
    public V call() throws Exception;
}
