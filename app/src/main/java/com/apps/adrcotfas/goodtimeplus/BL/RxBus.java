package com.apps.adrcotfas.goodtimeplus.BL;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxBus {

    public RxBus() {
    }

    private PublishSubject<Object> subject = PublishSubject.create();

    /**
     * Pass any event down to event listeners.
     */
    public void send(Object o) {
        subject.onNext(o);
    }

    /**
     * Subscribe to this Observable. On event, do something
     * e.g. replace a fragment
     */
    public Observable<Object> getEvents() {
        return subject;
    }
}
