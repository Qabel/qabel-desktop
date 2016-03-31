package de.qabel.desktop.inject;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(Creates.class)
public @interface Create {
    String name();
}
