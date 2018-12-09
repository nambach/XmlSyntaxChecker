package component.event;

import component.schema.Element;
import utils.MutationUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TagGroup {

    private XmlEvent openTag;
    private XmlEvent closeTag;

    private TagGroup(XmlEvent openTag, XmlEvent closeTag) {
        this.openTag = openTag;
        this.closeTag = closeTag;
    }

    private TagGroup(XmlEvent openTag) {
        this.openTag = openTag;
    }

    private static TagGroup createNormalTag(String tagName) {
        XmlEvent openTag = new XmlEvent(tagName, XmlEvent.TYPE.OPEN_TAG);
        XmlEvent closeTag = new XmlEvent(tagName, XmlEvent.TYPE.CLOSE_TAG);

        return new TagGroup(openTag, closeTag);
    }

    private static TagGroup createEmptyTag(String tagName) {
        XmlEvent emptyTag = new XmlEvent(tagName, XmlEvent.TYPE.EMPTY_TAG);

        return new TagGroup(emptyTag);
    }

    private TagGroup chain(TagGroup group) {
        if (closeTag != null) {
            closeTag.addNextEvent(group.getOpenTag());
        } else {
            openTag.addNextEvent(group.getOpenTag());
        }
        return group;
    }

    private void loop() {
        if (closeTag != null) {
            closeTag.addNextEvent(openTag);
        } else {
            openTag.addNextEvent(openTag);
        }
    }

    private void makeSimpleElement() {
        openTag.chain(XmlEvent.createContentEvent())
                .chain(closeTag);
    }

    private void chainInner(List<TagGroup> groupList) {
        if (closeTag == null) {
            return;
        }

        TagGroup stub = groupList.get(0);
        if (groupList.size() > 1) {
            for (int i = 1; i < groupList.size(); i++) {
                stub = stub.chain(groupList.get(i));
            }
        }

        //chain open tag
        this.openTag.addNextEvent(groupList.get(0).openTag);
        //chain close tag
        groupList.get(groupList.size() - 1).closeTag.addNextEvent(this.closeTag);

    }

    private void chainInner(TagGroup innerGroup) {
        if (closeTag == null) {
            return;
        }

        //chain open tag
        this.openTag.addNextEvent(innerGroup.openTag);
        //chain close tag
        innerGroup.closeTag.addNextEvent(this.closeTag);

    }

    public XmlEvent getOpenTag() {
        return openTag;
    }

    public XmlEvent getCloseTag() {
        return closeTag;
    }

    public static TagGroup convert(Element element) {
        TagGroup group;

        switch (element.getType()) {
            case Element.TYPE.TEXT_ONLY:
                group = createNormalTag(element.getName());
                group.makeSimpleElement();

                if (element.isUnbounded()) {
                    group.loop();
                }
                return group;
            case Element.TYPE.EMPTY:
                group = createEmptyTag(element.getName());
                break;
            case Element.TYPE.ELEMENT_ONLY:
                group = createNormalTag(element.getName());
                break;
            case Element.TYPE.MIXED:
                group = createNormalTag(element.getName());
                break;
            default:
                return null;
        }

        if (element.isUnbounded()) {
            group.loop();
        }

        List<TagGroup> groupList = new LinkedList<>();
        switch (element.getInnerType()) {
            case Element.INDICATOR.SEQUENCE:
                for (Element innerElement : element.getInnerElements()) {
                    groupList.add(convert(innerElement));
                }
                group.chainInner(groupList);
                break;
            case Element.INDICATOR.CHOICE:
                for (Element innerElement : element.getInnerElements()) {
                    TagGroup innerGroup = convert(innerElement);
                    group.chainInner(innerGroup);
                }
                break;
            case Element.INDICATOR.ALL:
                for (Element innerElement : element.getInnerElements()) {
                    groupList.add(convert(innerElement));
                }

                TagGroup[][] matrix = MutationUtils.mutate(groupList, TagGroup.class);
                for (TagGroup[] arr : matrix) {
                    group.chainInner(Arrays.asList(arr));
                }
        }

        return group;
    }
}
