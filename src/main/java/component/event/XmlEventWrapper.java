package component.event;

public class XmlEventWrapper {

    private XmlEvent preEvent;
    private XmlEvent postEvent;

    public XmlEventWrapper(XmlEvent in, XmlEvent out) {
        preEvent = new XmlEvent(null, XmlEvent.TYPE.TEMP);
        postEvent = new XmlEvent(null, XmlEvent.TYPE.TEMP);

        in.addNextEvent(preEvent);
        postEvent.addNextEvent(out);
    }

    public XmlEvent getPreEvent() {
        return preEvent;
    }

    public XmlEvent getPostEvent() {
        return postEvent;
    }
}
