package component.event;

import java.util.LinkedList;

public class XmlEventList extends LinkedList<XmlEvent> {

    public XmlEvent getContentEvent() {
        for (XmlEvent event : this) {
            if (event.isContent()) {
                return event;
            }
        }
        return null;
    }

    public XmlEvent getTagEvent(String name, String type) {
        for (XmlEvent event : this) {
            if (event.getName().equals(name) && event.getType().equals(type)) {
                return event;
            }
        }
        return null;
    }

    public XmlEvent getTagEvent(String type) {
        for (XmlEvent event : this) {
            if (event.getType().equals(type)) {
                return event;
            }
        }
        return null;
    }

    public boolean hasEvent(String eventType) {
        for (XmlEvent event : this) {
            if (event.getType().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    public XmlEvent getNext() {
        //Higher priority for closing document ASAP
        XmlEvent nextEvent = getTagEvent(XmlEvent.TYPE.CLOSE_TAG);
        if (nextEvent == null) {
            nextEvent = get(0);
        }
        return nextEvent;
    }
}
