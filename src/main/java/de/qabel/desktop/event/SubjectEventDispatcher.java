package de.qabel.desktop.event;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class SubjectEventDispatcher implements EventDispatcher {
    private Subject<Event, Event> subject = PublishSubject.create();

    @Override
    public Observable<Event> events() {
        return subject;
    }

    @Override
    public void push(Event event) {
        subject.onNext(event);
    }
}
