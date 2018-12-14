package xmlchecker;

import component.schema.data.ElementData;
import component.schema.template.Element;
import component.schema.template.SchemaEngine;

import java.util.*;

import static xmlchecker.SyntaxState.*;

public class XmlSyntaxChecker {

    private Stack<ElementData> stack;
    private Element rootElement;

    public XmlSyntaxChecker() {
        stack = new Stack<>();
    }

    public void setSchema(String path) {
        rootElement = SchemaEngine.getRootElement(path);

        Element grandElement = new Element(Element.TYPE.ELEMENT_ONLY, "document", null);
        grandElement.addChildElement(rootElement);
        rootElement.setParent(grandElement);

        ElementData grandData = new ElementData(grandElement);
        stack.push(grandData);
    }

    private boolean checkContentEmpty(StringBuilder content) {
        return content.toString().trim().equals("");
    }

    private boolean checkTagExist(String tagName) {
        return rootElement.containsDescendant(tagName);
    }

    public String check(String src) {
        src = src + " ";
        char[] reader = src.toCharArray();
        System.out.println(reader);
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
                        //OPEN TAG
                        String openTagName = openTag.toString().toLowerCase();

                        //Process if a close tag is missing: <title> The Alchemist <author>...
                        ElementData elementData = stack.peek();
                        if (elementData.isTextOnly() || elementData.isEmptyElement()) {
                            elementData.setContent(content.toString());
                            stack.pop();
                        } else {
                            if (elementData.isElementOnly() && !checkContentEmpty(content)) {
                                elementData.getAbandonedContents().push(content.toString().trim());
                            }
                        }

                        //Process if open tag is missing: <books>...<title> (misses the tag <book>)
                        elementData = stack.peek();
                        Element currentElement = elementData.getTemplateElement();

                        if (currentElement.containsDescendant(openTagName)) {
                            //Case 1: current encountered tag is inside top-stack tag, which is one of its descendants, e.g: <books>...<title>

                            //Fetch all the missing open tags, which altogether will form a parent tree from the current tag backwards to the top-stack tag
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

                        } else if (checkTagExist(openTagName)) {
                            //Case 2: current encountered tag is outside top-stack tag, maybe in same level, e.g: <title>...<author>

                            //pop out all tags until finding out the current tag's parent
                            do {
                                stack.pop();

                                elementData = stack.peek();
                                currentElement = elementData.getTemplateElement();

                            } while (!currentElement.containsChild(openTagName));

                            //Insert current tag as normal
                            Element targetElement = currentElement.getChildElement(openTagName);
                            ElementData newElementData = new ElementData(targetElement);

                            elementData.addInnerElement(newElementData);
                            stack.push(newElementData);

                            newElementData.getAttributes().putAll(attributes);
                        }

                        attributes.clear();
                    } else if (isCloseTag) {
                        //CLOSE TAG
                        String closeTagName = closeTag.toString().toLowerCase();

                        ElementData elementData = stack.peek();
                        Element currentElement = elementData.getTemplateElement();

                        if (currentElement.getName().equals(closeTagName)) {
                            //case 1: close tag normally
                            elementData.setContent(content.toString());
                            stack.pop();
                        } else if (currentElement.containsDescendant(closeTagName)) {
                            //case 2: current close tag is inside top-stack tag (children of top-stack tag)

                            Element targetElement = currentElement.getDescendantElement(closeTagName);
                            List<Element> missingElements = targetElement.getParentTreeExclusive(currentElement.getName());

                            for (Element missingElement : missingElements) {
                                ElementData newElementData = new ElementData(missingElement);
                                elementData.addInnerElement(newElementData);
                                stack.push(newElementData);
                                elementData = newElementData;
                            }

                            elementData.setContent(content.toString());
                            stack.pop();

                        } else if (checkTagExist(closeTagName)) {
                            //case 2: current close tag is outside top-stack tag (parent tag or a sibling of its parent)

                            if (!checkContentEmpty(content) && elementData.isTextOnly()) {
                                String contentStr = content.toString();
                                if (contentStr.contains("\n")) {
                                    elementData.setContent(contentStr.substring(0, contentStr.indexOf("\n")));
                                    content.setLength(0);
                                    content.append(contentStr.substring(contentStr.indexOf("\n")));
                                } else {
                                    elementData.setContent(contentStr);
                                    content.setLength(0);
                                }
                            }

                            if (currentElement.containsParent(closeTagName)) {
                                //case 3.a: current close tag is parent of top-stack tag
                                do {
                                    stack.pop();

                                    elementData = stack.peek();
                                    currentElement = elementData.getTemplateElement();

                                } while (!currentElement.containsChild(closeTagName));

                            } else {
                                //case 3.b: current close tag is not parent of top-stack tag, but a sibling of its parent
                                do {
                                    stack.pop();

                                    elementData = stack.peek();
                                    currentElement = elementData.getTemplateElement();

                                } while (!currentElement.containsChild(closeTagName));

                                Element targetElement = currentElement.getDescendantElement(closeTagName);
                                List<Element> missingElements = targetElement.getParentTreeExclusive(currentElement.getName());

                                for (Element missingElement : missingElements) {
                                    ElementData newElementData = new ElementData(missingElement);
                                    elementData.addInnerElement(newElementData);
                                    stack.push(newElementData);
                                    elementData = newElementData;
                                }

                                elementData.setContent(content.toString());
                                stack.pop();
                            }
                        }
                    }

                    content.setLength(0);
                    if (c == LT) {
                        state = OPEN_BRACKET;
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
            }//end switch state
        }//end for reader


        return stack.get(0).toString();
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
