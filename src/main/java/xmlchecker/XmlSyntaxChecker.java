package xmlchecker;

import schema.data.ElementData;
import schema.template.Element;
import schema.template.SchemaEngine;

import java.util.*;

import static schema.data.ElementData.ABSTRACT_ROOT_ELEMENT;
import static xmlchecker.SyntaxState.*;

public class XmlSyntaxChecker {

    private Stack<ElementData> stack;
    private Element rootElement;

    public XmlSyntaxChecker() {
        stack = new Stack<>();
    }

    public void setSchema(String path) {
        rootElement = SchemaEngine.getRootElement(path);

        Element abstractElement = new Element(Element.TYPE.ELEMENT_ONLY, ABSTRACT_ROOT_ELEMENT, null);
        abstractElement.addChildElement(rootElement);
        rootElement.setParent(abstractElement);

        ElementData abstractElementData = new ElementData(abstractElement);
        stack.push(abstractElementData);
    }

    private boolean checkTagExist(String tagName) {
        return rootElement.containsDescendant(tagName) || rootElement.getName().equals(tagName);
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

                        String[] arr = content.toString().split("\n");
                        List<String> contents = new LinkedList<>();
                        contents.addAll(Arrays.asList(arr));

                        ElementData elementData = stack.peek();
                        if (elementData.isTextOnly()) {
                            elementData.setContent(contents.remove(0));
                        }

                        for (String contentStr : contents) {
                            elementData.getAbandonedContents().push(contentStr);
                        }
                        content.setLength(0);
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
                    } else if (c == QUESTION_MARK) {
                        state = PROCESS_INSTRUCTION;
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
                    String openTagName = openTag.toString();
                    String closeTagName = closeTag.toString();

                    if (isOpenTag && checkTagExist(openTagName)) {
                        //OPEN TAG
                        ElementData elementData = stack.peek();
                        Element currentElement = elementData.getTemplateElement();

                        //pop out all tags until finding out the current tag's parent
                        while (!currentElement.containsDescendant(openTagName)) {
                            stack.pop();

                            elementData = stack.peek();
                            currentElement = elementData.getTemplateElement();
                        }

                        //Insert current tag as normal
                        Element targetElement = currentElement.getDescendantElement(openTagName);
                        List<Element> missingElements = targetElement.getParentTreeExclusive(currentElement.getName());

                        //Create the missing path
                        for (Element missingElement : missingElements) {
                            ElementData newElementData = new ElementData(missingElement);

                            elementData.addInnerElement(newElementData);
                            stack.push(newElementData);

                            elementData = newElementData;
                        }

                        elementData.getAttributes().putAll(attributes);

                        //don't account empty tag
                        if (elementData.isEmptyElement()) {
                            stack.pop();
                        }

                        attributes.clear();

                    } else if (isCloseTag && checkTagExist(closeTagName) && !closeTagName.equals(rootElement.getName())) {
                        //CLOSE TAG
                        ElementData elementData = stack.peek();
                        Element currentElement = elementData.getTemplateElement();
                        ElementData oldElementData = stack.peek();

                        while (!currentElement.containsDescendant(closeTagName)) {
                            stack.pop();

                            elementData = stack.peek();
                            currentElement = elementData.getTemplateElement();
                        }

                        if (!oldElementData.getTemplateElement().containsParent(closeTagName)
                                && !oldElementData.getName().equals(closeTagName)) {
                            //case 3.b: current close tag is not parent of top-stack tag, but a sibling of its parent

                            Element targetElement = currentElement.getDescendantElement(closeTagName);
                            List<Element> missingElements = targetElement.getParentTreeExclusive(currentElement.getName());

                            for (Element missingElement : missingElements) {
                                ElementData newElementData = new ElementData(missingElement);
                                elementData.addInnerElement(newElementData);
                                stack.push(newElementData);
                                elementData = newElementData;
                            }

                            String contentStr = !oldElementData.getAbandonedContents().isEmpty()
                                    ? oldElementData.getAbandonedContents().pop()
                                    : "";
                            elementData.setContent(contentStr);
                            stack.pop();
                        }
                    }

                    content.setLength(0);
                    if (c == LT) {
                        state = OPEN_BRACKET;

                        //For this case, without below code snippet, year would had got value "a" instead of empty string
                        //<book>a
                        //      b</authors></year>
                        //</book>
                        stack.peek().getAbandonedContents().push("");
                    } else {
                        state = CONTENT;
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

                case PROCESS_INSTRUCTION:
                    if (c == GT) {
                        state = CONTENT;
                    }
                    break;
            }//end switch state
        }//end for reader


        return stack.get(0).toString("");
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
