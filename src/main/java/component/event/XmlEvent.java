package component.event;

import java.util.LinkedList;
import java.util.List;

public class XmlEvent {

    public static class TYPE {
        public static final String OPEN_TAG = "openTag";
        public static final String CLOSE_TAG = "closeTag";
        public static final String EMPTY_TAG = "emptyTag";
        public static final String CONTENT = "content";
        public static final String TEMP = "temp";
    }

    private String name;
    private String type;

    private XmlEventList nextEvents;

    public XmlEvent(String name, String type) {
        this.name = name;
        this.type = type;
        this.nextEvents = new XmlEventList();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOpenTag() {
        return TYPE.OPEN_TAG.equals(type);
    }

    public boolean isCloseTag() {
        return TYPE.CLOSE_TAG.equals(type);
    }

    public boolean isEmptyTag() {
        return TYPE.EMPTY_TAG.equals(type);
    }

    public boolean isContent() {
        return TYPE.CONTENT.equals(type);
    }

    public boolean isTemporary() {
        return TYPE.TEMP.equals(type);
    }

    public XmlEvent addNextEvent(XmlEvent event) {
        this.nextEvents.add(event);
        return this;
    }

    public XmlEventList getNextEvents() {
        return nextEvents;
    }

    XmlEvent chain(XmlEvent event) {
        this.nextEvents.add(event);
        return event;
    }

    public static XmlEvent createContentEvent() {
        return new XmlEvent("", TYPE.CONTENT);
    }

    @Override
    public String toString() {
        return "XmlEvent{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
