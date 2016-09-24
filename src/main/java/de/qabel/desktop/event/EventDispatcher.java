package de.qabel.desktop.event;

import rx.Observable;

public interface EventDispatcher {
    Observable<Event> events();
    void push(Event event);
}
