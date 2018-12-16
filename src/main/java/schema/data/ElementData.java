package schema.data;

import schema.template.Element;
import xmlchecker.XmlSyntaxChecker;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static xmlchecker.SyntaxState.*;

public class ElementData {

    private Element templateElement;
    private String name;
    private String content = "";
    private LinkedHashMap<String, List<ElementData>> innerElements;
    private LinkedHashMap<String, String> attributes;
    private Stack<String> abandonedContents;

    public ElementData(Element element) {
        this.templateElement = element;
        this.name = element.getName();
        innerElements = new LinkedHashMap<>();

        attributes = new LinkedHashMap<>();
        abandonedContents = new Stack<>();
    }

    public void addInnerElement(ElementData elementData) {
        String elementName = elementData.name;
        if (innerElements.get(elementName) == null) {
            innerElements.put(elementName, new LinkedList<ElementData>());
        }

        innerElements.get(elementName).add(elementData);
    }

    public boolean isTextOnly() {
        return templateElement.getType().equals(Element.TYPE.TEXT_ONLY);
    }

    public boolean isElementOnly() {
        return templateElement.getType().equals(Element.TYPE.ELEMENT_ONLY);
    }

    public boolean isEmptyElement() {
        return templateElement.getType().equals(Element.TYPE.EMPTY);
    }

    public Element getTemplateElement() {
        return templateElement;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.trim();
    }

    public LinkedHashMap<String, List<ElementData>> getInnerElements() {
        return innerElements;
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public Stack<String> getAbandonedContents() {
        return abandonedContents;
    }

    private static final String INDENT_OFFSET = "    ";

    public String toString(String indent) {
        if (name.equals("document")) {
            return innerElements.get(templateElement.getChildElements().get(0).getName()).get(0).toString(indent);
        }

        StringBuilder builder = new StringBuilder();

        String attributeList = XmlSyntaxChecker.convert(attributes);

        if (Element.TYPE.EMPTY.equals(templateElement.getType())) {
            builder.append(indent).append(LT).append(name).append(attributeList).append(SLASH).append(GT);
        } else {
            String openTag = String.format("%s<%s%s>", indent, name, attributeList);
            String closeTag = String.format("</%s>\n", name);

            switch (templateElement.getType()) {
                case Element.TYPE.TEXT_ONLY:
                    builder.append(openTag).append(content).append(closeTag);
                    break;

                case Element.TYPE.ELEMENT_ONLY:
                    builder.append(openTag).append("\n");

                    for (Element element : templateElement.getChildElements()) {
                        List<ElementData> data = innerElements.get(element.getName());

                        if (data != null) {
                            for (ElementData elementData : data) {
                                builder.append(elementData.toString(indent + INDENT_OFFSET));
                            }
                        } else if (element.getMin() == 1) {
                            ElementData stubData = new ElementData(element);
                            builder.append(stubData.toString(indent + INDENT_OFFSET));
                        }
                    }

                    builder.append(indent).append(closeTag);
                    break;
            }
        }

        return builder.toString();
    }
}
