import component.event.TagGroup;
import component.schema.Element;
import component.schema.SchemaEngine;

public class TestSchema {

    public static void main(String[] args) {
        String path = "src/main/resources/static/xsd/crawling.xsd";
        Element rootElement = SchemaEngine.getRootElement(path);

        if (rootElement != null) {
            Element.iterateElement("", rootElement);
            TagGroup rootGroup = TagGroup.convert(rootElement);
            System.out.println(rootGroup.getOpenTag()
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(1)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(0)
                    .getNextEvents().get(1).getNextEvents().get(0).getNextEvents().get(1).equals(rootGroup.getCloseTag()));
        }
    }

}
