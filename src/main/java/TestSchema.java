import component.schema.template.Element;
import component.schema.template.SchemaEngine;

import java.util.List;

public class TestSchema {

    public static void main(String[] args) {
        String path = "src/main/resources/static/xsd/crawling.xsd";
        Element rootElement = SchemaEngine.getRootElement(path);

        if (rootElement != null) {
            Element.iterateElement("", rootElement);
        }
    }

}
