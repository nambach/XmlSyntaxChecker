package fsm;

import fsm.impl.Cache;

public interface Handler {
    void handle(Character input, Cache cache);
}
