package component.schema.template;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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
    private List<Element> childElements;
    private String innerType = INDICATOR.SEQUENCE;

    private Element parent;

    public Element(String type, String name, Element parent) {
        this.type = type;
        this.name = name;
        this.parent = parent;

        attributes = new LinkedList<>();
        childElements = new LinkedList<>();
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

    public List<Element> getChildElements() {
        return childElements;
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

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    public void addChildElement(Element element) {
        this.childElements.add(element);
    }

    public Element getChildElement(String name) {
        for (Element childElement : childElements) {
            if (childElement.name.equals(name)) {
                return childElement;
            }
        }
        return null;
    }

    public Element getDescendantElement(String name) {
        Element descendant = getChildElement(name);

        if (descendant != null) {
            return descendant;
        }

        for (Element childElement : childElements) {
            if (TYPE.ELEMENT_ONLY.equals(childElement.type)) {
                descendant = childElement.getDescendantElement(name);

                if (descendant != null) {
                    return descendant;
                }
            }
        }

        return null;
    }

    public boolean containsDescendant(String tagName) {
        return getDescendantElement(tagName) != null;
    }

    public boolean containsChild(String tagName) {
        return getChildElement(tagName) != null;
    }

    public boolean containsParent(String tagName) {
        Element temp = this.parent;
        while (temp != null) {
            if (temp.name.equals(tagName)) {
                return true;
            }
            temp = temp.parent;
        }
        return false;
    }

    /**
     *
     * @param parentName exclusive
     * @return a list of parent tree exclusive from parentName to current
     */
    public List<Element> getParentTreeExclusive(String parentName) {
        Stack<Element> temp = new Stack<>();
        List<Element> result = new LinkedList<>();

        Element stub = this;
        do {
            temp.push(stub);
            stub = stub.parent;
            if (stub == null) {
                return result;
            }
        } while (!stub.name.equals(parentName));

        while (!temp.isEmpty()) {
            result.add(temp.pop());
        }
        return result;
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
            for (Element innerElement : element.childElements) {
                iterateElement(indent + "  ", innerElement);
            }
        }

    }
}
