package xmlchecker2;

import fsm.Event;
import fsm.Handler;
import fsm.impl.Cache;
import fsm.impl.State;

import java.util.*;

import static xmlchecker2.SyntaxState.*;

public class XmlSyntaxReader {

    private List<State> stateList;
    private Map<String, State> stateMap;
    private State currentState;
    private Cache cache;

    public XmlSyntaxReader(Cache cache) {
        this.cache = cache;

        stateList = new LinkedList<>();
        defineState();
        stateMap = new HashMap<>();
        for (State state : stateList) {
            stateMap.put(state.getName(), state);
        }

        currentState = stateMap.get(CONTENT);
    }

    private void defineState() {
        Handler doNothing = new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
            }
        };
        final Handler startCollectingAttrName = new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.getAttrName().setLength(0);
                cache.getAttrName().append(input);
            }
        };
        final Handler processAloneAttr = new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                String attributeName = cache.getAttrName().toString();
                cache.getAttributes().put(attributeName, "true");
            }
        };
        Handler collectAttribute = new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                String attributeName = cache.getAttrName().toString();
                String attributeValue = cache.getAttrValue().toString();
                cache.getAttributes().put(attributeName, attributeValue);
            }
        };
        Handler collectTag = new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                if (cache.isOpenTag()) {
                    String openTagName = cache.getOpenTag().toString().toLowerCase();

                    if (INLINE_TAGS.contains(openTagName)) {
                        cache.setEmptyTag(true);
                    }
                    cache.getWriter().append(LT)
                            .append(openTagName)
                            .append(convert(cache.getAttributes()))
                            .append((cache.isEmptyTag() ? "/" : ""))
                            .append(GT);

                    cache.getAttributes().clear();

                    //STACK HERE: push open-tag
                    if (!cache.isEmptyTag()) {
                        cache.getStack().push(openTagName);
                    }

                } else if (cache.isCloseTag()) {
                    Stack<String> stack = cache.getStack();
                    StringBuilder writer = cache.getWriter();

                    //STACK HERE: pop out open-tag having the same name
                    String closeTagName = cache.getCloseTag().toString().toLowerCase();
                    //An open-tag is missing: <a><b><c>...</d>
                    //Then it must not appear in stack => ignore it

                    //A close-tag is missing: <a><b><c>...</a>
                    //Then it must appear in stack => process it
                    if (!stack.isEmpty() && stack.contains(closeTagName)) {
                        while (!stack.isEmpty() && !stack.peek().equals(closeTagName)) {
                            writer.append(LT)
                                    .append(SLASH)
                                    .append(stack.pop())
                                    .append(GT);
                        }
                        if (!stack.isEmpty() && stack.peek().equals(closeTagName)) {
                            writer.append(LT)
                                    .append(SLASH)
                                    .append(stack.pop())
                                    .append(GT);
                        }
                    } //end close-tag missing
                }
            }
        };

        Event sp = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return Character.isSpaceChar(input);
            }
        };
        Event startTagChars = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return isStartTagChars(input);
            }
        };
        Event tagChars = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return isTagChars(input);
            }
        };
        Event startAttrChars = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return isStartAttrChars(input);
            }
        };
        Event attrChars = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return isAttrChars(input);
            }
        };
        Event eq = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return input == EQ;
            }
        };
        final Event startQuot = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                if (input == D_QUOT || input == S_QUOT) {
                    cache.setQuote(input);
                    return true;
                }
                return false;
            }
        };
        final Event closeQuot = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return input.equals(cache.getQuote());
            }
        };
        Event notCloseQuot = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return !closeQuot.listen(input, cache);
            }
        };
        Event notSpAndNotGt = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return !Character.isSpaceChar(input) && input != GT && !startQuot.listen(input, cache);
            }
        };
        Event gt = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return input == GT;
            }
        };
        Event slash = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return input == SLASH;
            }
        };
        final Event lt = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return input == LT;
            }
        };
        Event notLt = new Event() {
            @Override
            public boolean listen(Character input, Cache cache) {
                return !lt.listen(input, cache);
            }
        };

        //content
        State content = new State(CONTENT);
        content.addTransition(OPEN_BRACKET, lt, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                String content = cache.getContent().toString().trim().replace("&", "&amp;");
                cache.getWriter().append(content);
            }
        }).addTransition(CONTENT, null, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.getContent().append(input);
            }
        });

        //openBracket
        State openBracket = new State(OPEN_BRACKET);
        openBracket.addTransition(OPEN_TAG_NAME, startTagChars, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.setOpenTag(true);
                cache.setCloseTag(false);
                cache.setEmptyTag(false);

                cache.getOpenTag().setLength(0);
                cache.getOpenTag().append(input);
            }
        }).addTransition(CLOSE_TAG_SLASH, slash, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.setOpenTag(false);
                cache.setCloseTag(true);
                cache.setEmptyTag(false);
            }
        });

        //openTagName
        State openTagName = new State(OPEN_TAG_NAME);
        openTagName.addTransition(CLOSE_BRACKET, gt, doNothing)
                .addTransition(EMPTY_SLASH, slash, doNothing)
                .addTransition(TAG_INNER, sp, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getAttributes().clear();
                    }
                })
                .addTransition(OPEN_TAG_NAME, tagChars, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getOpenTag().append(input);
                    }
                });

        //tagInner
        State tagInner = new State(TAG_INNER);
        tagInner.addTransition(ATTR_NAME, startAttrChars, startCollectingAttrName)
                .addTransition(CLOSE_BRACKET, gt, doNothing)
                .addTransition(EMPTY_SLASH, slash, doNothing)
                .addTransition(TAG_INNER, sp, doNothing);

        //attrName
        State attrName = new State(ATTR_NAME);
        attrName.addTransition(EMPTY_SLASH, slash, processAloneAttr)
                .addTransition(CLOSE_BRACKET, gt, processAloneAttr)
                .addTransition(EQUAL, eq, doNothing)
                .addTransition(EQUAL_WAIT, sp, doNothing)
                .addTransition(ATTR_NAME, attrChars, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getAttrName().append(input);
                    }
                });

        //equalWait
        State equalWait = new State(EQUAL_WAIT);
        equalWait.addTransition(EQUAL_WAIT, sp, doNothing)
                .addTransition(EQUAL, eq, doNothing)
                .addTransition(ATTR_NAME, startAttrChars, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        processAloneAttr.handle(input, cache);
                        startCollectingAttrName.handle(input, cache);
                    }
                });

        //equal
        State equal = new State(EQUAL);
        equal.addTransition(EQUAL, sp, doNothing)
                .addTransition(ATTR_VALUE_Q, startQuot, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getAttrValue().setLength(0);
                    }
                })
                .addTransition(ATTR_VALUE_NQ, notSpAndNotGt, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getAttrValue().setLength(0);
                        cache.getAttrValue().append(input);
                    }
                });

        //attrValueQ
        State attrValueQ = new State(ATTR_VALUE_Q);
        attrValueQ.addTransition(ATTR_VALUE_Q, notCloseQuot, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.getAttrValue().append(input);
            }
        }).addTransition(TAG_INNER, closeQuot, collectAttribute);

        //attrValueNQ
        State attrValueNQ = new State(ATTR_VALUE_NQ);
        attrValueNQ.addTransition(TAG_INNER, sp, collectAttribute)
                .addTransition(CLOSE_BRACKET, gt, collectAttribute)
                .addTransition(ATTR_VALUE_NQ, notSpAndNotGt, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getAttrValue().append(input);
                    }
                });

        //emptySlash
        State emptySlash = new State(EMPTY_SLASH);
        emptySlash.addTransition(CLOSE_BRACKET, gt, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.setEmptyTag(true);
            }
        });

        //closeBracket
        State closeBracket = new State(CLOSE_BRACKET);
        closeBracket.addFirstHandler(collectTag)
                .addTransition(OPEN_BRACKET, lt, doNothing)
                .addTransition(CONTENT, null, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getContent().setLength(0);
                        cache.getContent().append(input);
                    }
                });

        //closeTagSlash
        State closeTagSlash = new State(CLOSE_TAG_SLASH);
        closeTagSlash.addTransition(CLOSE_TAG_NAME, startTagChars, new Handler() {
            @Override
            public void handle(Character input, Cache cache) {
                cache.getCloseTag().setLength(0);
                cache.getCloseTag().append(input);
            }
        });

        //closeTagName
        State closeTagName = new State(CLOSE_TAG_NAME);
        closeTagName.addTransition(CLOSE_BRACKET, gt, doNothing)
                .addTransition(WAIT_END_TAG_CLOSE, sp, doNothing)
                .addTransition(CLOSE_TAG_NAME, tagChars, new Handler() {
                    @Override
                    public void handle(Character input, Cache cache) {
                        cache.getCloseTag().append(input);
                    }
                });

        //waitEndTagClose
        State waitEndTagClose = new State(WAIT_END_TAG_CLOSE);
        waitEndTagClose.addTransition(WAIT_END_TAG_CLOSE, sp, doNothing)
                .addTransition(CLOSE_BRACKET, gt, doNothing);


        stateList.add(content);
        stateList.add(openBracket);
        stateList.add(openTagName);
        stateList.add(tagInner);
        stateList.add(attrName);
        stateList.add(equalWait);
        stateList.add(equal);
        stateList.add(attrValueQ);
        stateList.add(attrValueNQ);
        stateList.add(emptySlash);
        stateList.add(closeBracket);
        stateList.add(closeTagSlash);
        stateList.add(closeTagName);
        stateList.add(waitEndTagClose);
    }

    public void listen(Character input) {
        String nextState = currentState.getName();
        try {
            nextState = currentState.listen(input, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentState = stateMap.get(nextState);
    }

    public String getCurrentState() {
        return currentState.getName();
    }
}
