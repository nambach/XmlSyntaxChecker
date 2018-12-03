package fsm.impl;

import fsm.Event;
import fsm.Handler;

public class Transition {
    Event event;
    Handler handler;

    public Transition(Event event, Handler handler) {
        this.event = event;
        this.handler = handler;
    }
}
