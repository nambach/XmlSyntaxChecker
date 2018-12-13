package component.event;

import component.schema.template.Element;

import static xmlchecker.SyntaxState.GT;
import static xmlchecker.SyntaxState.LT;
import static xmlchecker.SyntaxState.SLASH;

public class XmlEvent {

    public static class TYPE {
        public static final String OPEN_TAG = "openTag";
        public static final String CLOSE_TAG = "closeTag";
        public static final String EMPTY_TAG = "emptyTag";
        public static final String CONTENT = "content";
    }

    private Element element;

    private String name;
    private String type;
    private boolean buffer = false;

    private XmlEvent relatedEvent;
    private XmlEventList nextEvents;

    public XmlEvent(Element element, String type) {
        this.element = element;
        this.name = element.getName();
        this.type = type;
        this.nextEvents = new XmlEventList();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    public boolean isBuffer() {
        return buffer;
    }

    public void setBuffer(boolean buffer) {
        this.buffer = buffer;
    }

    public XmlEvent getRelatedEvent() {
        return relatedEvent;
    }

    public void setRelatedEvent(XmlEvent relatedEvent) {
        this.relatedEvent = relatedEvent;
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

    public static XmlEvent createContentEvent(Element element) {
        return new XmlEvent(element, TYPE.CONTENT);
    }

    @Override
    public String toString() {
        return "XmlEvent{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public void write(StringBuilder writer) {
        switch (type) {
            case TYPE.OPEN_TAG:
                writer.append(LT).append(name).append(GT);
                break;
            case TYPE.CLOSE_TAG:
                writer.append(LT).append(SLASH).append(name).append(GT);
                break;
            case TYPE.EMPTY_TAG:
                writer.append(LT).append(name).append(SLASH).append(GT);
                break;
        }
    }
}
