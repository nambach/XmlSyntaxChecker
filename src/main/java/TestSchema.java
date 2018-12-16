import schema.template.Element;
import schema.template.SchemaEngine;

public class TestSchema {

    public static void main(String[] args) {
        String path = "src/main/resources/static/xsd/crawling.xsd";
        Element rootElement = SchemaEngine.getRootElement(path);

        if (rootElement != null) {
            Element.iterateElement("", rootElement);
        }
    }

}
