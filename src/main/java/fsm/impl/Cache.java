package fsm.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Cache {

    private StringBuilder writer = new StringBuilder();

    private StringBuilder openTag = new StringBuilder();
    private StringBuilder closeTag = new StringBuilder();
    private StringBuilder content = new StringBuilder();

    private StringBuilder attrName = new StringBuilder();
    private StringBuilder attrValue = new StringBuilder();
    private Map<String, String> attributes = new HashMap<>();

    private Stack<String> stack = new Stack<>();

    private boolean isEmptyTag = false, isOpenTag = false, isCloseTag = false;

    private Character quote;

    public StringBuilder getWriter() {
        return writer;
    }

    public StringBuilder getOpenTag() {
        return openTag;
    }

    public StringBuilder getCloseTag() {
        return closeTag;
    }

    public StringBuilder getContent() {
        return content;
    }

    public StringBuilder getAttrName() {
        return attrName;
    }

    public StringBuilder getAttrValue() {
        return attrValue;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Stack<String> getStack() {
        return stack;
    }

    public boolean isEmptyTag() {
        return isEmptyTag;
    }

    public boolean isOpenTag() {
        return isOpenTag;
    }

    public boolean isCloseTag() {
        return isCloseTag;
    }

    public Character getQuote() {
        return quote;
    }

    public void setEmptyTag(boolean emptyTag) {
        isEmptyTag = emptyTag;
    }

    public void setOpenTag(boolean openTag) {
        isOpenTag = openTag;
    }

    public void setCloseTag(boolean closeTag) {
        isCloseTag = closeTag;
    }

    public void setQuote(Character quote) {
        this.quote = quote;
    }
}
