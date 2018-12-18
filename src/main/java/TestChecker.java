import utils.FileUtils;
import xmlchecker.XmlSyntaxChecker;

public class TestChecker {

    public static void main(String[] args) {
        XmlSyntaxChecker checker = new XmlSyntaxChecker();
        checker.setSchema("src/main/resources/static/xsd/book.xsd");

        String src = FileUtils.readTextContent("src/main/resources/static/xml/book.xml");
        System.out.println(checker.check(src));
    }
}
