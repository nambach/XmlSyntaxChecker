package fsm.impl;

import fsm.Event;
import fsm.Handler;

import java.util.HashMap;
import java.util.Map;

public class State {
    private String name;
    private Map<String, Transition> eventMap;

    private Transition defaultTransition;
    private String defaultNextState;

    private Handler firstHandler;

    public State(String name) {
        this.name = name;
        this.eventMap = new HashMap<>();
    }

    public State addTransition(String state, Event event, Handler handler) {
        Transition transition = new Transition(event, handler);
        if (event == null) {
            defaultTransition = transition;
            defaultNextState = state;
        } else {
            eventMap.put(state, transition);
        }
        return this;
    }

    public State addFirstHandler(Handler handler) {
        firstHandler = handler;
        return this;
    }

    public String getName() {
        return name;
    }

    public String listen(Character input, Cache cache) throws Exception {
        if (firstHandler != null) {
            firstHandler.handle(input, cache);
        }

        for (Map.Entry<String, Transition> entry : eventMap.entrySet()) {
            String state = entry.getKey();
            Event event = entry.getValue().event;
            Handler handler = entry.getValue().handler;

            if (event.listen(input, cache)) {
                handler.handle(input, cache);
                return state;
            }
        }
        //last "else"
        if (defaultNextState != null) {
            defaultTransition.handler.handle(input, cache);
            return defaultNextState;
        } else {
            System.out.println("Input: " + input);
            System.out.println("Cache: " + cache);
            throw new Exception("No defined state found!!");
        }
    }
}
