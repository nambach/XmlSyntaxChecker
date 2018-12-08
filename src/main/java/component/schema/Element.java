package component.schema;

import java.util.LinkedList;
import java.util.List;

public class Element {
    public static class TYPE {
        public static final String EMPTY = "empty";
        public static final String TEXT_ONLY = "textOnly";
        public static final String ELEMENT_ONLY = "elementOnly";
        public static final String MIXED = "mixed";
    }

    public static class INDICATOR {
        public static final String ALL = "all";
        public static final String SEQUENCE = "sequence";
        public static final String CHOICE = "choice";
    }

    private String type;
    private String name;
    private Integer min = 1;
    private Integer max = 1;
    private boolean unbounded = false;

    private List<Attribute> attributes;
    private List<Element> innerElements;
    private String innerType = INDICATOR.SEQUENCE;

    private Element parent;

    public Element(String type, String name, Element parent) {
        this.type = type;
        this.name = name;
        this.parent = parent;

        attributes = new LinkedList<>();
        innerElements = new LinkedList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public boolean isUnbounded() {
        return unbounded;
    }

    public void setUnbounded(boolean unbounded) {
        this.unbounded = unbounded;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Element> getInnerElements() {
        return innerElements;
    }

    public String getInnerType() {
        return innerType;
    }

    public void setInnerType(String innerType) {
        this.innerType = innerType;
    }

    public Element getParent() {
        return parent;
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    public void addInnerElement(Element element) {
        this.innerElements.add(element);
    }

    public static void iterateElement(String indent, Element element) {
        String max = element.unbounded ? "unbounded" : element.getMax().toString();
        System.out.println(String.format("%s<%s> %s %d-%s", indent, element.name, element.type, element.min, max));
        for (Attribute attribute : element.attributes) {
            String required = attribute.isRequired() ? "required" : "";
            System.out.println(String.format("%s- %s=\"%s\" %s", indent, attribute.getName(), attribute.getDefaultValue(), required));
        }
        if (TYPE.ELEMENT_ONLY.equals(element.type)) {
            System.out.println(String.format("%s%s", indent, element.innerType));
            for (Element innerElement : element.innerElements) {
                iterateElement(indent + "  ", innerElement);
            }
        }

    }
}
