import utils.FileUtils;
import xmlchecker.XmlSyntaxChecker;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestChecker {

    public static void main(String[] args) {
        XmlSyntaxChecker checker = new XmlSyntaxChecker();
        checker.setSchema("src/main/resources/static/xsd/book.xsd");

        String src = FileUtils.readTextContent("src/main/resources/static/xml/book.xml");
        System.out.println(checker.check(src));
    }

    private static void checkNormal() {
        XmlSyntaxChecker checker = new XmlSyntaxChecker();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Attribute không value", "<h1 checked>YEAH</h1>");
        map.put("Value không bọc trong cặp nháy", "<h1 aa=  aa><img a=a />YEAH</h1>");
        map.put("Attribute dính liền nhau", "<h1 a=\"1\"b='2'c=3>YEAH</h1>");
        map.put("Empty element", "<h1><img src=\"\"><br><hr/></h1>");
        map.put("Lỗi đóng mở thẻ", "<li><a>Sach Moi</a></h3>");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());

            System.out.println(entry.getValue());
            System.out.println(checker.check(entry.getValue()));

            System.out.println();
        }
    }
}
