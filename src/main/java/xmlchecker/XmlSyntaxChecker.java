package xmlchecker;

import component.schema.data.ElementData;
import component.schema.template.Element;
import component.schema.template.SchemaEngine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import static xmlchecker.SyntaxState.*;

public class XmlSyntaxChecker {

    private Stack<ElementData> stack;

    public XmlSyntaxChecker() {
        stack = new Stack<>();
    }

    public void setSchema(String path) {
        Element rootElement = SchemaEngine.getRootElement(path);

        Element grandElement = new Element(Element.TYPE.ELEMENT_ONLY, "document", null);
        grandElement.addChildElement(rootElement);

        ElementData grandData = new ElementData(grandElement);
        stack.push(grandData);
    }

    public String check(String src) {
        src = src + " ";
        char[] reader = src.toCharArray();

        StringBuilder openTag = new StringBuilder();
        boolean isEmptyTag = false, isOpenTag = false, isCloseTag = false;
        StringBuilder closeTag = new StringBuilder();
        StringBuilder attrName = new StringBuilder();
        StringBuilder attrValue = new StringBuilder();
        Map<String, String> attributes = new LinkedHashMap<>();

        StringBuilder content = new StringBuilder();

        String state = CONTENT;

        for (int i = 0; i < reader.length; i++) {
            char c = reader[i];

            switch (state) {
                case CONTENT:
                    if (c == LT) {
                        state = OPEN_BRACKET;
                        if (!content.toString().trim().equals("")) {
                            ElementData elementData = stack.peek();
                            if (elementData.getTemplateElement().getType().equals(Element.TYPE.TEXT_ONLY)) {
                                elementData.setContent(content.toString().trim());
                            }
                        }
                    } else {
                        content.append(c);
                    }
                    break;

                case OPEN_BRACKET:
                    if (isStartTagChars(c)) {
                        state = OPEN_TAG_NAME;

                        isOpenTag = true;
                        isCloseTag = false;
                        isEmptyTag = false;

                        openTag.setLength(0);
                        openTag.append(c);
                    } else if (c == SLASH) {
                        state = CLOSE_TAG_SLASH;

                        isOpenTag = false;
                        isCloseTag = true;
                        isEmptyTag = false;
                    }
                    break;

                case OPEN_TAG_NAME:
                    if (isTagChars(c)) { //loop
                        openTag.append(c);
                    } else if (isSpace(c)){
                        state = TAG_INNER;

                        attributes.clear();
                    } else if (c == GT) {
                        state = CLOSE_BRACKET;
                    } else if (c == SLASH) {
                        state = EMPTY_SLASH;
                    }
                    break;

                case TAG_INNER:
                    if (isSpace(c)) { //loop

                    } else if (isStartAttrChars(c)) {
                        state = ATTR_NAME;

                        attrName.setLength(0);
                        attrName.append(c);
                    } else if (c == GT) {
                        state = CLOSE_BRACKET;
                    } else if (c == SLASH) {
                        state = EMPTY_SLASH;
                    }
                    break;

                case ATTR_NAME:
                    if (isAttrChars(c)) { //loop
                        attrName.append(c);
                    } else if (c == EQ) {
                        state = EQUAL;
                    } else if (isSpace(c)) {
                        state = EQUAL_WAIT;

                    } else { //Exception
                        if (c == SLASH) {
                            attributes.put(attrName.toString(), "true");
                            state = EMPTY_SLASH;
                        } else if (c == GT) {
                            attributes.put(attrName.toString(), "true");
                            state = CLOSE_BRACKET;
                        }
                    }
                    break;

                case EQUAL_WAIT:
                    if (isSpace(c)) { //loop

                    } else if (c == EQ) {
                        state = EQUAL;

                    } else { //Exception
                        if (isStartAttrChars(c)) {
                            attributes.put(attrName.toString(), "true");
                            state = ATTR_NAME;

                            attrName.setLength(0);
                            attrName.append(c);
                        }
                    }
                    break;

                case EQUAL:
                    if (isSpace(c)) { //loop

                    } else if (c == D_QUOT || c == S_QUOT) {
                        quote = c;
                        state = ATTR_VALUE_Q;

                        attrValue.setLength(0);
                    } else if (!isSpace(c) && c != GT) {
                        state = ATTR_VALUE_NQ;

                        attrValue.setLength(0);
                        attrValue.append(c);
                    }
                    break;

                case ATTR_VALUE_Q:
                    if (c != quote) { //loop
                        attrValue.append(c);
                    } else if (c == quote) {
                        state = TAG_INNER;
                        attributes.put(attrName.toString(), attrValue.toString());
                    }
                    break;

                case ATTR_VALUE_NQ:
                    if (!isSpace(c) && c != GT) { //loop
                        attrValue.append(c);
                    } else if (isSpace(c)) {
                        state = TAG_INNER;
                        attributes.put(attrName.toString(), attrValue.toString());
                    } else if (c == GT) {
                        state = CLOSE_BRACKET;
                        attributes.put(attrName.toString(), attrValue.toString());
                    }
                    break;

                case EMPTY_SLASH:
                    if (c == GT) {
                        state = CLOSE_BRACKET;
                        isEmptyTag = true;
                    }
                    break;

                case CLOSE_BRACKET:
                    if (isOpenTag) {
                        String openTagName = openTag.toString().toLowerCase();

                        if (!stack.isEmpty()) {
                            ElementData parentData = stack.peek();
                            Element parent = parentData.getTemplateElement();

                            Element current = parent.getChildElement(openTagName);
                            ElementData currentData = new ElementData(current);

                            parentData.addInnerElement(currentData);
                            currentData.getAttributes().putAll(attributes);

                            stack.push(currentData);
                        }
                        attributes.clear();
                    } else if (isCloseTag) {
                        String closeTagName = closeTag.toString().toLowerCase();

                        if (stack.peek().getName().equals(closeTagName)) {
                            stack.pop();
                        }
                    }

                    if (c == LT) {
                        state = OPEN_BRACKET;
                    } else {
                        state = CONTENT;

                        content.setLength(0);
                        content.append(c);
                    }
                    break;

                case CLOSE_TAG_SLASH:
                    if (isStartTagChars(c)) {
                        state = CLOSE_TAG_NAME;

                        closeTag.setLength(0);
                        closeTag.append(c);
                    }
                    break;

                case CLOSE_TAG_NAME:
                    if (isTagChars(c)) { //loop
                        closeTag.append(c);
                    } else if (isSpace(c)) {
                        state = WAIT_END_TAG_CLOSE;
                    } else if (c == GT) {
                        state = CLOSE_BRACKET;
                    }
                    break;

                case WAIT_END_TAG_CLOSE:
                    if (isSpace(c)) { //loop

                    } else if (c == GT) {
                        state = CLOSE_BRACKET;
                    }
                    break;
            }//end switch state
        }//end for reader


        return stack.peek().getInnerElements().get("books").get(0).toString();
    }

    private Character quote;

    public static String convert(Map<String, String> attributes) {
        if (attributes.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String value = entry.getValue()
                    .replace("&", "&amp;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("'", "&apos;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;");

            builder.append(entry.getKey())
                    .append("=")
                    .append("\"").append(value).append("\"")
                    .append(" ");
        }

        String result =  builder.toString().trim();
        if (!result.equals("")) {
            result = " " + result;
        }

        return result;
    }
}
