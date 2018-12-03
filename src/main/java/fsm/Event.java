package fsm;

import fsm.impl.Cache;

public interface Event {
    boolean listen(Character input, Cache cache);
}
